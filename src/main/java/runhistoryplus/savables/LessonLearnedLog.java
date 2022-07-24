package runhistoryplus.savables;

import basemod.abstracts.CustomSavable;

import java.util.List;

public class LessonLearnedLog implements CustomSavable<List<List<String>>> {
    public static final String SaveKey = "LessonLearnedLog";
    public static List<List<String>> lesson_learned_per_floor;

    @Override
    public List<List<String>> onSave() {
        return LessonLearnedLog.lesson_learned_per_floor;
    }

    @Override
    public void onLoad(List<List<String>> lessonLearnedLog) {
        LessonLearnedLog.lesson_learned_per_floor = lessonLearnedLog;
    }
}
