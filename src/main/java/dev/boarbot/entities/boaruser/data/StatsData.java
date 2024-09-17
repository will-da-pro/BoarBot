package dev.boarbot.entities.boaruser.data;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record StatsData(
    long bucks,
    long highestBucks,
    int dailies,
    int dailiesMissed,
    Timestamp lastDailyTimestamp,
    String lastBoar,
    String favBoar,
    long totalBoars,
    long highestBoars,
    int uniques,
    int highestUniques,
    int boarStreak,
    int highestStreak,
    boolean notificationsOn,
    long blessings,
    int highestBlessings,
    int streakBless,
    int highestStreakBless,
    int questBless,
    int highestQuestBless,
    int uniqueBless,
    int highestUniqueBless,
    int otherBless,
    int highestOtherBless,
    int powerupAttempts,
    int powerupWins,
    int perfectPowerups,
    int fastestPowerup,
    double avgPowerupPlacement,
    List<String> bestPrompts,
    Map<String, Integer> powAmts,
    Map<String, Integer> peakPowAmts,
    Map<String, Integer> powUsed,
    int miraclesActive,
    int miracleRolls,
    int miraclesMostUsed,
    int miracleBestBucks,
    String miracleBestRarity,
    String lastTransmuteBoar,
    Map<String, Integer> rarityTransmutes,
    String lastCloneBoar,
    Map<String, Integer> rarityClones,
    int giftHandicap,
    int giftsOpened,
    int giftFastest,
    int giftBestBucks,
    String giftBestRarity,
    int questsCompleted,
    int fullQuestsCompleted,
    long fastestFullQuest,
    boolean questAutoClaim,
    int easyQuests,
    int mediumQuests,
    int hardQuests,
    int veryHardQuests
) {
    public StatsData() {
        this(
            0,
            0,
            0,
            0,
            null,
            null,
            null,
            0,
            0,
            0,
            0,
            0,
            0,
            false,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            new ArrayList<>(),
            new HashMap<>(),
            new HashMap<>(),
            new HashMap<>(),
            0,
            0,
            0,
            0,
            null,
            null,
            new HashMap<>(),
            null,
            new HashMap<>(),
            0,
            0,
            0,
            0,
            null,
            0,
            0,
            0,
            true,
            0,
            0,
            0,
            0
        );
    }
}