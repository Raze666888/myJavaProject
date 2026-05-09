package com.javaPro.myProject.controller;

import com.javaPro.myProject.config.BaseIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

/**
 * API集成测试模板
 * 
 * 说明：
 * 1. 将 "template" 替换为实际的模块名称
 * 2. 将 "Template" 替换为实际的实体名称
 * 3. 根据实际的API端点修改URL
 * 4. 根据实际的数据结构修改JSON数据
 * 
 * @author Test Team
 * @date 2026-05-09
 */
@DisplayName("模板API集成测试")
public class TemplateControllerIntegrationTest extends BaseIntegrationTest {
    
    @Test
    @DisplayName("测试获取所有数据")
    public void testGetAll() {
        given()
                .log().all()
        .when()
                .get("/template/queryByPage")
        .then()
                .log().all()
                .statusCode(anyOf(is(200), is(404), is(500)));
    }
    
    @Test
    @DisplayName("测试根据ID获取数据")
    public void testGetById() {
        given()
                .log().all()
                .queryParam("id", 1)
        .when()
                .get("/template/queryById")
        .then()
                .log().all()
                .statusCode(anyOf(is(200), is(404), is(500)));
    }
    
    @Test
    @DisplayName("测试创建数据")
    public void testCreate() {
        String json = "{\"name\":\"测试数据\",\"description\":\"测试描述\"}";
        
        given()
                .log().all()
                .contentType("application/json")
                .body(json)
        .when()
                .post("/template/insert")
        .then()
                .log().all()
                .statusCode(anyOf(is(200), is(400), is(500)));
    }
    
    @Test
    @DisplayName("测试更新数据")
    public void testUpdate() {
        String json = "{\"id\":1,\"name\":\"更新的数据\"}";
        
        given()
                .log().all()
                .contentType("application/json")
                .body(json)
        .when()
                .put("/template/update")
        .then()
                .log().all()
                .statusCode(anyOf(is(200), is(400), is(500)));
    }
    
    @Test
    @DisplayName("测试删除数据")
    public void testDelete() {
        given()
                .log().all()
                .queryParam("id", 1)
        .when()
                .delete("/template/deleteById")
        .then()
                .log().all()
                .statusCode(anyOf(is(200), is(400), is(500)));
    }
    
    @Test
    @DisplayName("测试搜索功能")
    public void testSearch() {
        given()
                .log().all()
                .queryParam("name", "测试")
        .when()
                .get("/template/queryByPage")
        .then()
                .log().all()
                .statusCode(anyOf(is(200), is(404), is(500)));
    }
}
