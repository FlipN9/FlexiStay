package com.yany.flexistay.repository;

import com.yany.flexistay.model.Stay;
import com.yany.flexistay.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StayRepository extends JpaRepository<Stay, Long> {
    List<Stay> findByHost(User user); // Spring自动帮我们生成SQL返回结果

    Stay findByIdAndHost(Long id, User host);

    List<Stay> findByIdInAndGuestNumberGreaterThanEqual(List<Long> ids, int guestNumber);
}
