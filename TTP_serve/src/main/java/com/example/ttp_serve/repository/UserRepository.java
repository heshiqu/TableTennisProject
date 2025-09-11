package com.example.ttp_serve.repository;

import com.example.ttp_serve.entity.User;
import com.example.ttp_serve.enums.UserStatus;
import com.example.ttp_serve.enums.UserType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 根据用户名查找用户
    Optional<User> findByUsername(String username);

    // 检查用户名是否存在
    boolean existsByUsername(String username);

    // 根据用户类型和校区ID查找用户
    List<User> findByUserTypeAndCampusId(UserType userType, Long campusId);

    // 根据用户类型、校区ID和状态查找用户
    List<User> findByUserTypeAndCampusIdAndStatus(UserType userType, Long campusId, UserStatus status);

    // 根据校区ID查找用户（分页）
    Page<User> findByCampusId(Long campusId,Pageable pageable);

    // 根据用户类型查找用户（分页）
    Page<User> findByUserType(UserType userType,Pageable pageable);

    // 根据用户类型和状态查找用户
    List<User> findByUserTypeAndStatus(UserType userType, UserStatus status);

    // 根据手机号查找用户
    Optional<User> findByPhone(String phone);

    // 根据邮箱查找用户
    Optional<User> findByEmail(String email);

    // 根据用户名或真实姓名模糊查询
    @Query("SELECT u FROM User u WHERE u.username LIKE %:keyword% OR u.realName LIKE %:keyword%")
    List<User> findByUsernameOrRealNameContaining(@Param("keyword") String keyword);

    // 统计校区内的用户数量
    Long countByCampusId(Long campusId);

    // 统计校区内特定类型的用户数量
    Long countByCampusIdAndUserType(Long campusId, UserType userType);

    Long countByUserType(UserType userType);

    Long countByStatus(UserStatus status);

    @Query("SELECT COUNT(u) FROM User u WHERE u.campus.id = :campusId AND u.userType IN :userTypes")
    Long countByCampusIdAndUserTypeIn(@Param("campusId") Long campusId,
                                      @Param("userTypes") List<UserType> userTypes);
}