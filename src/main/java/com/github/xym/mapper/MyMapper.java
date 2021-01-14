package com.github.xym.mapper;

import com.github.xym.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MyMapper {

    @Select("select * from user where username = #{username}")
    User findUserByUsername(@Param("username") String username);

    @Insert("insert into user (username,encrypted_password,created_at,updated_at) " +
            "values (#{username},#{encode},now(),now())")
    void save(@Param("username") String username, @Param("encode") String encode);
}
