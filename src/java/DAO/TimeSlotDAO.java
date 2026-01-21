package DAO;

import Model.TimeSlot;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public class TimeSlotDAO extends DBContext {
    public List<TimeSlot> listAll() throws Exception {
        String sql = """
                SELECT slot_id, name, start_time, end_time
                FROM dbo.time_slots
                ORDER BY start_time
                """;
        List<TimeSlot> result = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) result.add(map(rs));
        }
        return result;
    }

    public TimeSlot findById(int slotId) throws Exception {
        String sql = "SELECT slot_id, name, start_time, end_time FROM dbo.time_slots WHERE slot_id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, slotId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return map(rs);
            }
        }
    }

    public int create(TimeSlot s) throws Exception {
        String sql = "INSERT INTO dbo.time_slots(name, start_time, end_time) VALUES(?, ?, ?)";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getName());
            ps.setTime(2, Time.valueOf(s.getStartTime()));
            ps.setTime(3, Time.valueOf(s.getEndTime()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) throw new IllegalStateException("No generated key returned");
                return rs.getInt(1);
            }
        }
    }

    public void update(TimeSlot s) throws Exception {
        String sql = "UPDATE dbo.time_slots SET name=?, start_time=?, end_time=? WHERE slot_id=?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, s.getName());
            ps.setTime(2, Time.valueOf(s.getStartTime()));
            ps.setTime(3, Time.valueOf(s.getEndTime()));
            ps.setInt(4, s.getSlotId());
            ps.executeUpdate();
        }
    }

    public void delete(int slotId) throws Exception {
        String sql = "DELETE FROM dbo.time_slots WHERE slot_id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, slotId);
            ps.executeUpdate();
        }
    }

    private static TimeSlot map(ResultSet rs) throws Exception {
        TimeSlot s = new TimeSlot();
        s.setSlotId(rs.getInt("slot_id"));
        s.setName(rs.getString("name"));
        Time st = rs.getTime("start_time");
        Time et = rs.getTime("end_time");
        if (st != null) s.setStartTime(st.toLocalTime());
        if (et != null) s.setEndTime(et.toLocalTime());
        return s;
    }
}

