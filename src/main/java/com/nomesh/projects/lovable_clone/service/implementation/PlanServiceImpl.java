package com.nomesh.projects.lovable_clone.service.implementation;

import com.nomesh.projects.lovable_clone.dto.plan.CreatePlanRequest;
import com.nomesh.projects.lovable_clone.dto.plan.PlanResponse;
import com.nomesh.projects.lovable_clone.entity.Plan;
import com.nomesh.projects.lovable_clone.mapper.PlanMapper;
import com.nomesh.projects.lovable_clone.repository.PlanRepository;
import com.nomesh.projects.lovable_clone.service.PlanService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Transactional
public class PlanServiceImpl implements PlanService {

    PlanMapper planMapper;
    PlanRepository planRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PlanResponse> getAllActivePlans() {
        return planMapper.toPlanResponseList(
                planRepository.findByActiveTrueAndPaymentPriceIdIsNotNull()
        );
    }

    @Override
    public PlanResponse createPlan(CreatePlanRequest createPlanRequest) {
        Plan plan = planMapper.toPlan(createPlanRequest);
        return planMapper.toPlanResponse(
                planRepository.save(plan)
        );
    }
}
