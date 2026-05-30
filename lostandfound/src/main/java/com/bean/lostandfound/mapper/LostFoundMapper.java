package com.bean.lostandfound.mapper;

import com.bean.lostandfound.pojo.dto.LostSearchDTO;
import com.bean.lostandfound.pojo.entity.LostFound;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface LostFoundMapper {
    List<LostFound> selectByCondition(LostSearchDTO lostSearchDTO);
    @Select("select * from lost_found where id=#{id}")
    LostFound getById(Integer id);

    void insert(LostFound lostFound);

    void update(LostFound originalLostFound);

    void updateStatus(LostFound lostFound);
    /**
     * 查询7天前创建且状态为已审核的失物信息
     * @return 失物信息列表
     */
    List<LostFound> selectExpiredLostFound();
    @Delete("delete from lost_found where id=#{id}")
    void deleteById(Integer id);

    @Select("select count(*) from lost_found where user_id = #{userId}")
    int countByUserId(Integer userId);
}
