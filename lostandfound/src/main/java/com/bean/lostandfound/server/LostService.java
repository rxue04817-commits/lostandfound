package com.bean.lostandfound.server;

import com.bean.lostandfound.pojo.dto.LostFoundDTO;
import com.bean.lostandfound.pojo.dto.LostSearchDTO;
import com.bean.lostandfound.pojo.dto.UpdateLostFoundDTO;
import com.bean.lostandfound.pojo.vo.LostFoundDetailVO;
import com.bean.lostandfound.result.PageResult;

public interface LostService {
    PageResult getLostFoundList(LostSearchDTO lostSearchDTO);

    LostFoundDetailVO getLostFoundDetail(Integer id);

    void publishLostFound(LostFoundDTO lostFoundDTO, Integer userId);

    void updateLostFound(Integer id, UpdateLostFoundDTO updateLostFoundDTO, Integer currentUserId);

    void updateLostFoundStatus(Integer id, Integer status, Integer userId, Integer userRole);

    void deleteById(Integer id, Integer userId, Integer userRole);
}
