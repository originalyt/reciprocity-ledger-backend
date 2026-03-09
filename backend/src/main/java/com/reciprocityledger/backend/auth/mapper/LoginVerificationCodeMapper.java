package com.reciprocityledger.backend.auth.mapper;

import com.reciprocityledger.backend.auth.entity.LoginVerificationCode;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface LoginVerificationCodeMapper {

    @Insert("insert into login_verification_code(id, phone, verification_code, expires_at, used) values(#{id}, #{phone}, #{verificationCode}, #{expiresAt}, #{used})")
    int insert(LoginVerificationCode verificationCode);

    @Select("select id, phone, verification_code, expires_at, used, created_at from login_verification_code where phone = #{phone} and verification_code = #{verificationCode} and used = false and expires_at > now() order by id desc limit 1")
    LoginVerificationCode selectValidCode(@Param("phone") String phone, @Param("verificationCode") String verificationCode);

    @Update("update login_verification_code set used = true where id = #{id}")
    int markUsed(@Param("id") Long id);
}
