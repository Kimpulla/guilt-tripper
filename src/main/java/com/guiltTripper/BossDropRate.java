package com.guiltTripper;

import java.util.HashMap;
import java.util.Map;

public class BossDropRate {
    private static final Map<String, Integer> dropRates = new HashMap<>();

    static {
        dropRates.put("Chaos Elemental", 300);
        dropRates.put("Dagannoth Supreme", 5000);
        dropRates.put("Dagannoth Prime", 5000);
        dropRates.put("Dagannoth Rex", 5000);
        dropRates.put("Kree'arra", 5000);
        dropRates.put("General Graardor", 5000);
        dropRates.put("Commander Zilyana", 5000);
        dropRates.put("K'ril Tsutsaroth", 5000);
        dropRates.put("Giant mole", 3000);
        dropRates.put("King Black Dragon", 3000);
        dropRates.put("Kalphite Queen", 3000);
        dropRates.put("Thermonuclear smoke devil", 3000);
        dropRates.put("Kraken", 3000);
        dropRates.put("Corporeal Beast", 5000);
        dropRates.put("Zulrah", 4000);
        dropRates.put("Venenatis", 1500);
        dropRates.put("Spindel", 2800);
        dropRates.put("Callisto", 1500);
        dropRates.put("Artio", 2800);
        dropRates.put("Vet'ion", 1500);
        dropRates.put("Calvar'ion", 2800);
        dropRates.put("Scorpia", 2016);
        dropRates.put("TzTok-Jad", 200);
        dropRates.put("Cerberus", 3000);
        dropRates.put("Skotizo", 65);
        dropRates.put("TzKal-Zuk", 100);
        dropRates.put("Dusk", 3000);
        dropRates.put("Vorkath", 3000);
        dropRates.put("Alchemical Hydra", 3000);
        dropRates.put("Sarachnis", 3000);
        dropRates.put("Zalcano", 2250);
        dropRates.put("The Nightmare", 1000);
        dropRates.put("Phosani's Nightmare", 1400);
        dropRates.put("Nex", 500);
        dropRates.put("Phantom Muspah", 2500);
        dropRates.put("The Whisperer", 2000);
        dropRates.put("Vardorvis", 3000);
        dropRates.put("The Leviathan", 2500);
        dropRates.put("Duke Sucellus", 2500);
        dropRates.put("Scurrius", 3000);
    }

    public static int getDropRate(String npcName) {
        return dropRates.getOrDefault(npcName, 5000);
    }
}
