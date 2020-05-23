/*
 * Copyright 2020 Régis BERTHELOT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ro.uvt.regisb.magpie.utils;

/**
 * Constants.
 * Ensemble of final static fields of recurring values through the project.
 */
public class C {
    public final static String SEPARATOR = ":";

    public final static String PLAYER_AID = "magpie_player";
    public final static String MOOD_AID = "magpie_mood";
    public final static String CONTENTMNGR_AID = "magpie_contentmanager";
    public final static String PLAYLIST_AID = "magpie_playlist";
    public final static String PREFERENCES_AID = "magpie_preferences";
    public final static String PROCESSES_AID = "magpie_processesmonitor";
    public final static String TIME_AID = "magpie_time";

    public final static String CONFIGURATION_ACL = "configuration";
    public final static String PLAYLIST_EXPANSION_ACL = "expand" + SEPARATOR;
    public final static String BATCH_SIZE_ACL = "batch" + SEPARATOR;
    public final static String TIMESLOT_ADD_ACL = "timeslot" + SEPARATOR + "add" + SEPARATOR;
    public final static String TIMESLOT_REMOVE_ACL = "timeslot" + SEPARATOR + "remove" + SEPARATOR;
    public final static String TIMESLOT_OBJ_ACL = "timeslot" + SEPARATOR;
    public static final String PROCESS_REMOVE_ACL = "process" + SEPARATOR + "remove" + SEPARATOR;
    public static final String PROCESS_ADD_ACL = "process" + SEPARATOR + "add" + SEPARATOR;
    public static final String PROCESS_OBJ_ACL = "process" + SEPARATOR;
    public static final String MOOD_ACL = "mood" + SEPARATOR;
    public static final String CONTENT_ACL = "content" + SEPARATOR;
    public static final String CONFIGURATION_RESTORE_ACL = "conf" + SEPARATOR;
    public static final String MANAGEMENT_FAILURE_ACL = "mngr" + SEPARATOR + "fail" + SEPARATOR;

    public static final String DEFAULT_BATCH_SIZE = "2";
}
