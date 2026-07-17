package org.example.back.service;

import org.example.back.dto.AppointmentCreateRequest;
import java.util.Map;

public interface AppointmentService {

    Map<String, Object> create(AppointmentCreateRequest request, Long userId);

    Map<String, Object> list(Long userId, Integer status, Integer pageNum, Integer pageSize);

    Map<String, Object> detail(Long id, Long userId);

    void cancel(Long id, Long userId);
}
