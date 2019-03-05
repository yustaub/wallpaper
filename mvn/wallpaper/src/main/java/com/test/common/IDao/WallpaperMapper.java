package com.test.common.IDao;

import com.test.common.domain.Wallpaper;

public interface WallpaperMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Wallpaper record);

    int insertSelective(Wallpaper record);

    Wallpaper selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Wallpaper record);

    int updateByPrimaryKey(Wallpaper record);
}