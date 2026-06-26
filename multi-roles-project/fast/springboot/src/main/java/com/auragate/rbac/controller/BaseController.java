package com.auragate.rbac.controller;

import com.auragate.rbac.domain.AjaxResult;
import com.auragate.rbac.domain.TableDataInfo;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

/**
 * web层通用数据处理
 * 这是所有控制器的"爸爸"(基类), 其他控制器可以继承它
 * 目的: 把常用的方法仔仔这里, 避免每个控制器重复写
 */
public class BaseController {
    /**
     * 返回成功(无数据)
     */
    public AjaxResult success() {
        return AjaxResult.success();
    }

    /**
     * 返回错误(无数据)
     */
    public AjaxResult error() {
        return AjaxResult.error();
    }

    /**
     * 返回成功(带消息)
     */
    public AjaxResult success(String msg) {
        return AjaxResult.success(msg);
    }

    /**
     * 返回成功(带数据)
     */
    public AjaxResult success(Object data) {
        return AjaxResult.success(data);
    }

    /**
     * 返回错误(带消息)
     */
    public AjaxResult error(String msg) {
        return AjaxResult.error(msg);
    }

    /**
     * 根据受到影响的行数判断操作是否成功
     * @param rows 数据库操作影响的行数
     * @return 成功或者失败
     * 例子:
     *  int rows = userService.deleteUser(id) //删除用户
     *  return toAjax(rows) //如果rows > 0代表删除成功, 否则返回失败
     */
    protected AjaxResult toAjax(int rows) {
        return rows > 0 ? AjaxResult.success() : AjaxResult.error();
    }

    /**
     * 设置请求分页数据, 启动分页功能
     * 使用场景: 在查询列表数据的方法开头调用
     */
    protected void startPage() {
        //获取 当前HTTP请求
        //RequestContextHolder是spring提供的, 可以在任何地方获取当前请求
        HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        //获取并且转换分页参数
        int pageNum = Integer.parseInt(req.getParameter("pageNum"));
        int pageSize = Integer.parseInt(req.getParameter("pageSize"));

        //PageHelper.startPage() 是PageHelper插件的核心方法
        //它会自动在接下来的第一次查询sql中加上limit语句
        //相当于自动给sql加上: limit (pageNum - 1)*pageSize, pageSize
        //.setReasonable(true); 合理化分页
        //什么叫合理化分页?
        //  -当pageNum<=0时, 自动设置为第1页
        //  -当pageNum超过总页数时, 自动设置为最后一页
        PageHelper.startPage(pageNum, pageSize).setReasonable(true);
    }

    /**
     * 插入分页数据
     */
    protected <T>TableDataInfo getDataTable(List<T> list) {
        TableDataInfo dataInfo = new TableDataInfo();
        dataInfo.setCode(200);
        dataInfo.setMsg("查询列表成功");
        dataInfo.setRows(list);
        dataInfo.setTotal(new PageInfo<>(list).getTotal());
        return dataInfo;
    }

}
