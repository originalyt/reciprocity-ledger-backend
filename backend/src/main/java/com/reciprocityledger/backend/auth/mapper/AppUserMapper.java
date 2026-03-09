package com.reciprocityledger.backend.auth.mapper;

import com.reciprocityledger.backend.auth.entity.AppUser;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

@Mapper
public interface AppUserMapper {

    @Select("select id, email, email_verified, phone, phone_verified, password_hash, password_salt, nickname, status, last_login_at, created_at, updated_at, created_by, updated_by from app_user where email = #{email} limit 1")
    AppUser selectByEmail(@Param("email") String email);

    @Select("select id, email, email_verified, phone, phone_verified, password_hash, password_salt, nickname, status, last_login_at, created_at, updated_at, created_by, updated_by from app_user where id = #{id} limit 1")
    AppUser selectById(@Param("id") Long id);

    @Insert("insert into app_user(id, email, email_verified, phone, phone_verified, password_hash, password_salt, nickname, status, last_login_at, created_by, updated_by) values(#{id}, #{email}, #{emailVerified}, #{phone}, #{phoneVerified}, #{passwordHash}, #{passwordSalt}, #{nickname}, #{status}, #{lastLoginAt}, #{createdBy}, #{updatedBy})")
    int insert(AppUser appUser);

    @Update("update app_user set last_login_at = #{lastLoginAt}, updated_at = now(), updated_by = #{userId} where id = #{userId}")
    int updateLastLogin(@Param("userId") Long userId, @Param("lastLoginAt") LocalDateTime lastLoginAt);
}
