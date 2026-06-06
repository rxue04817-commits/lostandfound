package com.bean.lostandfound.mapper;

import com.bean.lostandfound.pojo.entity.Claim;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ClaimMapper {
    void insert(Claim claim);

    @Select("SELECT * FROM claim WHERE id = #{id}")
    Claim findById(Integer id);

    @Update("UPDATE claim SET status = #{status}, reject_reason = #{rejectReason}, updated_at = NOW() WHERE id = #{id}")
    void updateStatus(@Param("id") Integer id, @Param("status") Integer status, @Param("rejectReason") String rejectReason);

    List<Claim> findByClaimerId(@Param("claimerId") Integer claimerId);

    List<Claim> findByLostFoundOwnerId(@Param("ownerId") Integer ownerId);
    
    @Select("SELECT count(*) FROM claim WHERE lost_found_id = #{lostFoundId} AND status = 0")
    int countPendingByLostFoundId(Integer lostFoundId);
    
    @Select("SELECT * FROM claim WHERE lost_found_id = #{lostFoundId} AND status != 2")
    List<Claim> findActiveByLostFoundId(Integer lostFoundId);

    @Select("SELECT * FROM claim WHERE lost_found_id = #{lostFoundId} AND claimer_id = #{claimerId} AND status = 0")
    Claim findPendingByLostFoundIdAndClaimerId(@Param("lostFoundId") Integer lostFoundId, @Param("claimerId") Integer claimerId);

    @Select("SELECT * FROM claim WHERE lost_found_id = #{lostFoundId} AND claimer_id = #{claimerId} ORDER BY created_at DESC LIMIT 1")
    Claim findLatestByLostFoundIdAndClaimerId(@Param("lostFoundId") Integer lostFoundId, @Param("claimerId") Integer claimerId);

    @Update("UPDATE claim SET status = 2, reject_reason = '其他认领已通过', updated_at = NOW() WHERE lost_found_id = #{lostFoundId} AND status = 0")
    void rejectOtherPendingClaims(Integer lostFoundId);
}