/** Copyright (c) 2022-2023, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.behaviors;

import cn.harryh.arkpets.ArkConfig;
import cn.harryh.arkpets.utils.AnimData;
import cn.harryh.arkpets.utils.AnimData.AnimAutoData;

import java.util.HashMap;


abstract public class Behavior {
    public AnimAutoData[] action_list;
    public String[] anim_list;
    protected ArkConfig config;
    protected float deltaMin;
    protected float timeRec;
    protected float duraRec;
    protected int idxRec;
    
    /** Character Behavior Controller Instance.
     * @param $config ArkConfig object.
     * @param $animList The animation name list.
     */
    public Behavior(ArkConfig $config, String[] $animList) {
        action_list = null;
        anim_list = $animList;
        config = $config;
        deltaMin = 0.5f;
        timeRec = 0;
        duraRec = 0;
        idxRec = 0;
    }

    /** Get a random animation.
     * @param $deltaTime The delta time.
     * @return AnimData object.
     */
    public final AnimData autoCtrl(float $deltaTime) {
        duraRec += $deltaTime;
        timeRec += $deltaTime;
        if (timeRec >= deltaMin) {
            timeRec = 0f;
            if (duraRec >= action_list[idxRec].DURATION_MIN) {
                // Now try to change action
                duraRec = 0f;
                idxRec = getRandomAction();
                return action_list[idxRec].ANIM;
            }
        }
        return null;
    }

    /** Randomly select an action to play.
     * @return The index of the action.
     */
    int getRandomAction() {
        // Calculate the sum of all action's weight
        int weight_sum = 0;
        for (AnimAutoData i: action_list) {
            weight_sum += i.WEIGHT;
        }
        // Random select a weight
        int weight_select = (int)Math.ceil(Math.random() * weight_sum);
        // Figure out which action the weight referred
        weight_sum = 0;
        for (int j = 0; j < action_list.length; j++) {
            weight_sum += action_list[j].WEIGHT;
            if (weight_select <= weight_sum)
                return j;
        }
        return -1;
    }

    /** Search the most possible animation name.
     */
    String getProperAnimName(String $wanted, String[] $notWanted) {
        HashMap<String, Integer> map = new HashMap<>();
        for (String s : anim_list) {
            if (s.contains($wanted)) {
                Integer i = 0;
                map.put(s, i);
                for (String t : $notWanted)
                    if (s.contains(t))
                        i += 1;
            }
        }
        String minK = "";
        Integer minV = Integer.MAX_VALUE;
        for (HashMap.Entry<String, Integer> entry : map.entrySet()) {
            Integer value = entry.getValue();
            if (value < minV) {
                minV = value;
                minK = entry.getKey();
            }
        }
        return minK;
    }

    /** Whether the provided animation list match this behavior class.
     * @param animList The animation name list.
     * @return true=match, false=mismatch.
     */
    abstract boolean match(String[] animList);

    /** Select a matched behavior object from a behavior-list.
     * @param $animList A list contains the name of animations.
     * @param $candidateBehaviors A list contains the Behavior objects to be selected.
     * @return Behavior object.
     */
    public static Behavior selectBehavior(String[] $animList, Behavior[] $candidateBehaviors) {
        for (Behavior $candidateBehavior : $candidateBehaviors)
            if ($candidateBehavior.match($animList))
                return $candidateBehavior;
        return null;
    }

    /** Get the default animation.
     * @return AnimData object.
     */
    public AnimData defaultAnim() {
        return null;
    }

    /** Get the animation when mouse-down.
     * @return AnimData object.
     */
    public AnimData clickStart() {
        return null;
    }

    /** Get the animation when mouse-up.
     * @return AnimData object.
     */
    public AnimData clickEnd() {
        return null;
    }

    /** Get the animation when user start dragging.
     * @return AnimData object.
     */
    public AnimData dragStart() {
        return null;
    }

    /** Get the animation when user end dragging.
     * @return AnimData object.
     */
    public AnimData dragEnd() {
        return null;
    }

    /** Get the animation when character dropped.
     * @return AnimData object.
     */
    public AnimData drop() {
        return null;
    }
}
