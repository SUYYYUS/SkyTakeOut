package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.UserHolder;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @Override
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        //加密处理
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }
        //判断当前账号状态
        if (employee.getStatus().equals(StatusConstant.DISABLE)) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 新增员工
     * @param employeeDTO
     */
    @Override
    public Result addEmployee(EmployeeDTO employeeDTO){
        //创建实体类对象
        Employee employee = new Employee();
        //复制
        BeanUtils.copyProperties(employeeDTO, employee);
        //添加状态，默认为1
        employee.setStatus(StatusConstant.ENABLE);
        //设置默认密码
        String password = DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes());
        employee.setPassword(password);
        //设置创建时间
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());
        //设置创建人
        employee.setCreateUser(UserHolder.getCurrentId());
        employee.setUpdateUser(UserHolder.getCurrentId());
        //添加
        employeeMapper.insert(employee);
        return Result.success("新增成功");
    }

    @Override
    public Result<PageResult> getEmployees(EmployeePageQueryDTO employeePageQueryDTO) {
        //判断DTO是否为空
        if(employeePageQueryDTO == null){
            return Result.error("不能为空");
        }
        //开始分页查询
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());
        Page<Employee> employeeByQuery = employeeMapper.getEmployeeByQuery(employeePageQueryDTO);
        //封装对象
        PageResult pageResult = new PageResult();
        long total = employeeByQuery.getTotal();
        // TODO:查询员工时应该返回vo，不用把员工的全部信息全部返回，比如密码身份证之类的
        List<Employee> result = employeeByQuery.getResult();
        pageResult.setTotal(total);
        pageResult.setRecords(result);
        //返回结果
        return Result.success(pageResult);
    }

    /**
     * 启动禁用员工
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, long id) {
        //创建一个实体对象
        Employee employee = Employee.builder()
                .id(id)
                .status(status)
                .build();
            employeeMapper.updateEmployeeById(employee);
    }

}
