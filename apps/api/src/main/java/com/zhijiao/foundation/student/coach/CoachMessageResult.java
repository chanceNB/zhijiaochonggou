package com.zhijiao.foundation.student.coach;

import com.zhijiao.foundation.knowledge.Citation;

import java.util.List;

public record CoachMessageResult(String assistantMessage, List<Citation> citations,
                                 List<CoachAction> actions, RagStatus ragStatus) {
    public record CoachAction(String type, String label) {
    }
}
