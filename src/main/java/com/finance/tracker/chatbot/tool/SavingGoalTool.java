package com.finance.tracker.chatbot.tool;

import com.finance.tracker.entity.SavingsGoal;
import com.finance.tracker.repository.SavingsGoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SavingGoalTool extends AbstractAiTool {

    private static final Set<String> GOAL_KEYWORD = Set.of(
            "goal", "goals", "saving for", "saving goal", "target", "fund",
            "vacation fund", "emergency fund", "dream", "progress", "how close",
            "how far", "milestone"
    );

    private  final SavingsGoalRepository goalRepository;


    @Override
    public String name() {
        return "SavingGoalTool";
    }

    @Override
    public boolean supports(String question) {
        String q = normalize(question);
        return GOAL_KEYWORD.stream().anyMatch(q::contains);
    }

    @Override
    public ToolResult execute(String userId, String question) {
        List<SavingsGoal> goals = goalRepository.findByUserIdAndStatusIn(userId, List.of("ACTIVE", "PAUSED"));

        if (goals.isEmpty()) {
           return ToolResult.builder()
                   .handled(true)
                   .toolName(name())
                   .data("No active saving goals Found")
                   .build();
        }

        StringBuilder data = new StringBuilder();
        data.append("Saving Goals: \n");

        for (SavingsGoal goal : goals) {
            BigDecimal progress = goal.getProgressPercentage();
            BigDecimal remaining = goal.getTargetAmount().subtract(goal.getCurrentAmount());

            data.append("\n\u2022 ").append(goal.getGoalName());
            data.append(" [").append(goal.getStatus()).append("] \n");
            data.append(" saved: $").append(goal.getCurrentAmount());
            data.append(" / $").append(goal.getTargetAmount());
            data.append(" (").append(progress).append("% complete)\n");
            data.append("  Remaining: $").append(remaining).append("\n");

            if (goal.getTargetDate() != null) {
                long daysLeft= ChronoUnit.DAYS.between(LocalDate.now(), goal.getTargetDate());
                if (daysLeft > 0) {
                    data.append(" Target date : ").append(goal.getTargetDate())
                            .append(" (").append(daysLeft).append(" days left");
                } else {
                    data.append(" Target date : ").append(goal.getTargetDate())
                            .append(" Overdue By ").append(Math.abs(daysLeft)).append(" days)\n");
                }
            }
        }
        return ToolResult.builder()
                .handled(true)
                .toolName(name())
                .data(data.toString())
                .build();
    }
}
