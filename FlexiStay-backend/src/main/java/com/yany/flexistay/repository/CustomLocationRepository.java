package com.yany.flexistay.repository;

import java.util.List;

public interface CustomLocationRepository {
    List<Long> searchByDistance(double lat, double lon, String distance);
}

