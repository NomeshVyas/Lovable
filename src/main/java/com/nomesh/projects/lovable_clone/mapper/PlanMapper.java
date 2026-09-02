package com.nomesh.projects.lovable_clone.mapper;

import com.nomesh.projects.lovable_clone.dto.plan.CreatePlanRequest;
import com.nomesh.projects.lovable_clone.dto.plan.PlanResponse;
import com.nomesh.projects.lovable_clone.entity.Plan;
import org.mapstruct.Mapper;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PlanMapper {

    Plan toPlan(CreatePlanRequest planRequest);

    PlanResponse toPlanResponse(Plan plan);

    List<PlanResponse> toPlanResponseList(List<Plan> plans);
}
