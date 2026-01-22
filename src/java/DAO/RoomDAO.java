package DAO;

import Model.Room;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO extends DBContext {
    public List<Room> listAll(String statusFilter, String q) throws Exception {
        String sql = """
                SELECT room_id, room_code, room_name, capacity, status
                FROM dbo.rooms
                WHERE (? IS NULL OR status = ?)
                  AND (? IS NULL OR room_code LIKE ? OR room_name LIKE ?)
                ORDER BY room_id ASC
                """;
        List<Room> result = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            String like = q == null ? null : "%" + q + "%";
            ps.setString(1, statusFilter);
            ps.setString(2, statusFilter);
            ps.setString(3, q);
            ps.setString(4, like);
            ps.setString(5, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(map(rs));
            }
        }
        return result;
    }

    public List<Room> listActive() throws Exception {
        return listAll("ACTIVE", null);
    }

    public Room findById(int roomId) throws Exception {
        String sql = """
                SELECT room_id, room_code, room_name, capacity, status
                FROM dbo.rooms
                WHERE room_id = ?
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return map(rs);
            }
        }
    }

    public Room findByCode(String roomCode) throws Exception {
        String sql = """
                SELECT room_id, room_code, room_name, capacity, status
                FROM dbo.rooms
                WHERE room_code = ?
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, roomCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return map(rs);
            }
        }
    }

    public int create(Room r) throws Exception {
        String sql = """
                INSERT INTO dbo.rooms(room_code, room_name, capacity, status)
                VALUES(?, ?, ?, ?)
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, r, false);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) throw new IllegalStateException("No generated key returned");
                return rs.getInt(1);
            }
        }
    }

    public void update(Room r) throws Exception {
        String sql = """
                UPDATE dbo.rooms
                SET room_code = ?, room_name = ?, capacity = ?, status = ?
                WHERE room_id = ?
                """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            bind(ps, r, true);
            ps.executeUpdate();
        }
    }

    public void setStatus(int roomId, String status) throws Exception {
        String sql = "UPDATE dbo.rooms SET status = ? WHERE room_id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, roomId);
            ps.executeUpdate();
        }
    }

    private static Room map(ResultSet rs) throws Exception {
        Room r = new Room();
        r.setRoomId(rs.getInt("room_id"));
        r.setRoomCode(rs.getString("room_code"));
        r.setRoomName(rs.getString("room_name"));
        r.setCapacity(rs.getInt("capacity"));
        r.setStatus(rs.getString("status"));
        return r;
    }

    private static void bind(PreparedStatement ps, Room r, boolean includeIdAtEnd) throws Exception {
        ps.setString(1, r.getRoomCode());
        ps.setString(2, r.getRoomName());
        ps.setInt(3, r.getCapacity());
        ps.setString(4, r.getStatus());
        if (includeIdAtEnd) ps.setInt(5, r.getRoomId());
    }

    public DeleteResult deleteSafe(int roomId) throws Exception {
        try (Connection con = getConnection()) {
            con.setAutoCommit(false);
            try {
                int scheduleCount = count(con, "SELECT COUNT(*) FROM dbo.class_schedules WHERE room_id = ?", roomId);
                int sessionCount = count(con, "SELECT COUNT(*) FROM dbo.class_sessions WHERE room_id = ?", roomId);
                if (scheduleCount > 0 || sessionCount > 0) {
                    con.rollback();
                    return DeleteResult.fail("Không thể xóa phòng: đang được dùng trong lịch/buổi học (schedules=" + scheduleCount + ", sessions=" + sessionCount + ").");
                }

                exec(con, "UPDATE dbo.classes SET room_id = NULL WHERE room_id = ?", roomId);
                int deleted = exec(con, "DELETE FROM dbo.rooms WHERE room_id = ?", roomId);
                con.commit();
                if (deleted <= 0) return DeleteResult.fail("Không tìm thấy phòng để xóa.");
                return DeleteResult.ok("Đã xóa phòng.");
            } catch (Exception ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    private static int count(Connection con, String sql, int id) throws Exception {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private static int exec(Connection con, String sql, int id) throws Exception {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }
}
