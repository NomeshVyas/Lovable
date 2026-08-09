package com.nomesh.projects.lovable_clone.service;

import com.nomesh.projects.lovable_clone.dto.plan.PlanResponse;

import java.util.List;

public interface PlanService {
    List<PlanResponse> getAllActivePlans();
}
