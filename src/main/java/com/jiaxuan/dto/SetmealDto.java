package com.jiaxuan.dto;


import com.jiaxuan.domain.Setmeal;
import com.jiaxuan.domain.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
