package com.reciprocityledger.backend.auth.mapper;

import com.reciprocityledger.backend.auth.entity.UserRefreshToken;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserRefreshTokenMapper {

    @Insert("insert into user_refresh_token(id, user_id, refresh_token, device_info, expires_at, revoked) values(#{id}, #{userId}, #{refreshToken}, #{deviceInfo}, #{expiresAt}, #{revoked})")
    int insert(UserRefreshToken refreshToken);

    @Select("select id, user_id, refresh_token, device_info, expires_at, revoked, created_at, updated_at from user_refresh_token where refresh_token = #{refreshToken} limit 1")
    UserRefreshToken selectByToken(@Param("refreshToken") String refreshToken);

    @Update("update user_refresh_token set revoked = true, updated_at = now() where refresh_token = #{refreshToken}")
    int revokeByToken(@Param("refreshToken") String refreshToken);
}
