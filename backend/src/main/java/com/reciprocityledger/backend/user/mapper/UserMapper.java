package com.reciprocityledger.backend.user.mapper;

import com.reciprocityledger.backend.user.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper {

    @Select("select id, email, password, nickname, avatar_url, status, last_login_time, create_time, update_time from rl_user where email = #{email} limit 1")
    User selectByEmail(@Param("email") String email);

    @Select("select id, email, password, nickname, avatar_url, status, last_login_time, create_time, update_time from rl_user where id = #{userId} limit 1")
    User selectById(@Param("userId") String userId);

    @Insert("insert into rl_user(id, email, password, nickname, avatar_url, status, last_login_time) values(#{id}, #{email}, #{password}, #{nickname}, #{avatarUrl}, #{status}, #{lastLoginTime})")
    int insert(User user);

    @Update("update rl_user set last_login_time = now() where id = #{userId}")
    int updateLastLoginTime(@Param("userId") String userId);

    @Update("update rl_user set nickname = #{nickname}, avatar_url = #{avatarUrl}, update_time = now() where id = #{userId}")
    int update(User user);

    @Update("update rl_user set password = #{password}, update_time = now() where id = #{userId}")
    int updatePassword(@Param("userId") String userId, @Param("password") String password);
}
