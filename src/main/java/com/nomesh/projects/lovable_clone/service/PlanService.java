package com.nomesh.projects.lovable_clone.service;

import com.nomesh.projects.lovable_clone.dto.plan.CreatePlanRequest;
import com.nomesh.projects.lovable_clone.dto.plan.PlanResponse;
import com.nomesh.projects.lovable_clone.entity.Plan;

import java.util.List;

public interface PlanService {

    List<PlanResponse> getAllActivePlans();

    PlanResponse createPlan(CreatePlanRequest createPlanRequest);
}
