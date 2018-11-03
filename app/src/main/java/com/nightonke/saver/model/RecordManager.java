package com.nightonke.saver.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.github.johnpersano.supertoasts.SuperToast;
import com.nightonke.saver.BuildConfig;
import com.nightonke.saver.R;
import com.nightonke.saver.activity.UtcCoinApplication;
import com.nightonke.saver.db.DB;
import com.nightonke.saver.util.UtcCoinToast;
import com.nightonke.saver.util.UtcCoinUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.DeleteListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

/**
 * Created by 伟平 on 2015/10/20.
 */

public class RecordManager {

    private static RecordManager recordManager = null;

    private static DB db;

    // the selected values in list activity
    public static Double SELECTED_SUM;
    public static List<UtcCoinRecord> SELECTED_RECORDS;

    public static Integer SUM;
    public static List<UtcCoinRecord> RECORDS;
    public static List<Tag> TAGS;
    public static Map<Integer, String> TAG_NAMES;

    public static boolean RANDOM_DATA = false;
    private final int RANDOM_DATA_NUMBER_ON_EACH_DAY = 3;
    private final int RANDOM_DATA_EXPENSE_ON_EACH_DAY = 30;

    private static boolean FIRST_TIME = true;

    public static int SAVE_TAG_ERROR_DATABASE_ERROR = -1;
    public static int SAVE_TAG_ERROR_DUPLICATED_NAME = -2;

    public static int DELETE_TAG_ERROR_DATABASE_ERROR = -1;
    public static int DELETE_TAG_ERROR_TAG_REFERENCE = -2;

// constructor//////////////////////////////////////////////////////////////////////////////////////
    private RecordManager(Context context) {
        try {
            db = db.getInstance(context);
            if (BuildConfig.DEBUG) if (BuildConfig.DEBUG) Log.d("UtcCoin", "db.getInstance(context) S");
        } catch(IOException e) {
            e.printStackTrace();
        }
        if (FIRST_TIME) {
// if the app starts firstly, create tags///////////////////////////////////////////////////////////
            SharedPreferences preferences =
                    context.getSharedPreferences("Values", Context.MODE_PRIVATE);
            if (preferences.getBoolean("FIRST_TIME", true)) {
                createTags();
                SharedPreferences.Editor editor =
                        context.getSharedPreferences("Values", Context.MODE_PRIVATE).edit();
                editor.putBoolean("FIRST_TIME", false);
                editor.commit();
            }
        }
        if (RANDOM_DATA) {

            SharedPreferences preferences =
                    context.getSharedPreferences("Values", Context.MODE_PRIVATE);
            if (preferences.getBoolean("RANDOM", false)) {
                return;
            }

            randomDataCreater();

            SharedPreferences.Editor editor =
                    context.getSharedPreferences("Values", Context.MODE_PRIVATE).edit();
            editor.putBoolean("RANDOM", true);
            editor.commit();

        }
    }

// getInstance//////////////////////////////////////////////////////////////////////////////////////
    public synchronized static RecordManager getInstance(Context context) {
        if (RECORDS == null || TAGS == null || TAG_NAMES == null || SUM == null || recordManager == null) {
            SUM = 0;
            RECORDS = new LinkedList<>();
            TAGS = new LinkedList<>();
            TAG_NAMES = new HashMap<>();
            recordManager = new RecordManager(context);

            db.getData();

            if (BuildConfig.DEBUG) {
                if (BuildConfig.DEBUG) Log.d("UtcCoin", "Load " + RECORDS.size() + " records S");
                if (BuildConfig.DEBUG) Log.d("UtcCoin", "Load " + TAGS.size() + " tags S");
            }

            TAGS.add(0, new Tag(-1, "Sum Histogram", -4));
            TAGS.add(0, new Tag(-2, "Sum Pie", -5));

            for (Tag tag : TAGS) TAG_NAMES.put(tag.getId(), tag.getName());

            sortTAGS();
        }
        return recordManager;
    }

// saveRecord///////////////////////////////////////////////////////////////////////////////////////
    public static long saveRecord(final UtcCoinRecord coCoinRecord) {
        long insertId = -1;
        // this is a new coCoinRecord, which is not uploaded
        coCoinRecord.setIsUploaded(false);
//        User user = BmobUser.getCurrentUser(UtcCoinApplication.getAppContext(), User.class);
//        if (user != null) coCoinRecord.setUserId(user.getObjectId());
//        else coCoinRecord.setUserId(null);
        if (BuildConfig.DEBUG)
            if (BuildConfig.DEBUG) Log.d("UtcCoin", "recordManager.saveRecord: Save " + coCoinRecord.toString() + " S");
        insertId = db.saveRecord(coCoinRecord);
        if (insertId == -1) {
            if (BuildConfig.DEBUG)
                if (BuildConfig.DEBUG) Log.d("UtcCoin", "recordManager.saveRecord: Save the above coCoinRecord FAIL!");
            UtcCoinToast.getInstance()
                    .showToast(R.string.save_failed_locale, SuperToast.Background.RED);
        } else {
            if (BuildConfig.DEBUG)
                if (BuildConfig.DEBUG) Log.d("UtcCoin", "recordManager.saveRecord: Save the above coCoinRecord SUCCESSFULLY!");
            RECORDS.add(coCoinRecord);
            SUM += (int) coCoinRecord.getMoney();
//            if (user != null) {
//                // already login
//                coCoinRecord.save(UtcCoinApplication.getAppContext(), new SaveListener() {
//                    @Override
//                    public void onSuccess() {
//                        if (BuildConfig.DEBUG)
//                            if (BuildConfig.DEBUG) Log.d("UtcCoin", "recordManager.saveRecord: Save online " + coCoinRecord.toString() + " S");
//                        coCoinRecord.setIsUploaded(true);
//                        coCoinRecord.setLocalObjectId(coCoinRecord.getObjectId());
//                        db.updateRecord(coCoinRecord);
//                        UtcCoinToast.getInstance()
//                                .showToast(R.string.save_successfully_online, SuperToast.Background.BLUE);
//                    }
//                    @Override
//                    public void onFailure(int code, String msg) {
//                        if (BuildConfig.DEBUG)
//                            if (BuildConfig.DEBUG) Log.d("UtcCoin", "recordManager.saveRecord: Save online " + coCoinRecord.toString() + " F");
//                        if (BuildConfig.DEBUG)
//                            if (BuildConfig.DEBUG) Log.d("UtcCoin", "recordManager.saveRecord: Save online msg: " + msg + " code " + code);
//                        UtcCoinToast.getInstance()
//                                .showToast(R.string.save_failed_online, SuperToast.Background.RED);
//                    }
//                });
//            } else {
//                UtcCoinToast.getInstance()
//                        .showToast(R.string.save_successfully_locale, SuperToast.Background.BLUE);
//            }
            UtcCoinToast.getInstance()
                    .showToast(R.string.save_successfully_locale, SuperToast.Background.BLUE);
        }
        return insertId;
    }

// save tag/////////////////////////////////////////////////////////////////////////////////////////
    public static int saveTag(Tag tag) {
        int insertId = -1;
        if (BuildConfig.DEBUG) {
            if (BuildConfig.DEBUG) Log.d("UtcCoin", "recordManager.saveTag: " + tag.toString());
        }
        boolean duplicatedName = false;
        for (Tag t : TAGS) {
            if (t.getName().equals(tag.getName())) {
                duplicatedName = true;
                break;
            }
        }
        if (duplicatedName) {
            return SAVE_TAG_ERROR_DUPLICATED_NAME;
        }
        insertId = db.saveTag(tag);
        if (insertId == -1) {
            if (BuildConfig.DEBUG) {
                if (BuildConfig.DEBUG) Log.d("UtcCoin", "Save the above tag FAIL!");
                return SAVE_TAG_ERROR_DATABASE_ERROR;
            }
        } else {
            if (BuildConfig.DEBUG) {
                if (BuildConfig.DEBUG) Log.d("UtcCoin", "Save the above tag SUCCESSFULLY!");
            }
            TAGS.add(tag);
            TAG_NAMES.put(tag.getId(), tag.getName());
            sortTAGS();
        }
        return insertId;
    }

// delete a coCoinRecord//////////////////////////////////////////////////////////////////////////////////
    public static long deleteRecord(final UtcCoinRecord coCoinRecord, boolean deleteInList) {
        long deletedNumber = db.deleteRecord(coCoinRecord.getId());
        if (deletedNumber > 0) {
            if (BuildConfig.DEBUG) Log.d("UtcCoin",
                    "recordManager.deleteRecord: Delete " + coCoinRecord.toString() + " S");
            User user = BmobUser.getCurrentUser(UtcCoinApplication.getAppContext(), User.class);
            // if we can delete the coCoinRecord from server
//            if (user != null && coCoinRecord.getLocalObjectId() != null) {
//                coCoinRecord.delete(UtcCoinApplication.getAppContext(), new DeleteListener() {
//                    @Override
//                    public void onSuccess() {
//                        if (BuildConfig.DEBUG) {
//                            if (BuildConfig.DEBUG) Log.d("UtcCoin",
//                                    "recordManager.deleteRecord: Delete online " + coCoinRecord.toString() + " S");
//                        }
//                        UtcCoinToast.getInstance()
//                                .showToast(R.string.delete_successfully_online, SuperToast.Background.BLUE);
//                    }
//                    @Override
//                    public void onFailure(int code, String msg) {
//                        if (BuildConfig.DEBUG) {
//                            if (BuildConfig.DEBUG) Log.d("UtcCoin",
//                                    "recordManager.deleteRecord: Delete online " + coCoinRecord.toString() + " F");
//                        }
//                        UtcCoinToast.getInstance()
//                                .showToast(R.string.delete_failed_online, SuperToast.Background.RED);
//                    }
//                });
//            } else {
//                UtcCoinToast.getInstance()
//                        .showToast(R.string.delete_successfully_locale, SuperToast.Background.BLUE);
//            }
            UtcCoinToast.getInstance()
                    .showToast(R.string.delete_successfully_locale, SuperToast.Background.BLUE);
            // update RECORDS list and SUM
            SUM -= (int) coCoinRecord.getMoney();
            if (deleteInList) {
                int size = RECORDS.size();
                for (int i = 0; i < RECORDS.size(); i++) {
                    if (RECORDS.get(i).getId() == coCoinRecord.getId()) {
                        RECORDS.remove(i);
                        if (BuildConfig.DEBUG) Log.d("UtcCoin",
                                "recordManager.deleteRecord: Delete in RECORD " + coCoinRecord.toString() + " S");
                        break;
                    }
                }
            }
        } else {
            if (BuildConfig.DEBUG) Log.d("UtcCoin",
                    "recordManager.deleteRecord: Delete " + coCoinRecord.toString() + " F");
            UtcCoinToast.getInstance()
                    .showToast(R.string.delete_failed_locale, SuperToast.Background.RED);
        }


        return coCoinRecord.getId();
    }

    public static int deleteTag(int id) {
        int deletedId = -1;
        if (BuildConfig.DEBUG) Log.d("UtcCoin",
                "Manager: Delete tag: " + "Tag(id = " + id + ", deletedId = " + deletedId + ")");
        boolean tagReference = false;
        for (UtcCoinRecord coCoinRecord : RECORDS) {
            if (coCoinRecord.getTag() == id) {
                tagReference = true;
                break;
            }
        }
        if (tagReference) {
            return DELETE_TAG_ERROR_TAG_REFERENCE;
        }
        deletedId = db.deleteTag(id);
        if (deletedId == -1) {
            if (BuildConfig.DEBUG) Log.d("UtcCoin", "Delete the above tag FAIL!");
            return DELETE_TAG_ERROR_DATABASE_ERROR;
        } else {
            if (BuildConfig.DEBUG) Log.d("UtcCoin", "Delete the above tag SUCCESSFULLY!");
            for (Tag tag : TAGS) {
                if (tag.getId() == deletedId) {
                    TAGS.remove(tag);
                    break;
                }
            }
            TAG_NAMES.remove(id);
            sortTAGS();
        }
        return deletedId;
    }

    private static int p;
    public static long updateRecord(final UtcCoinRecord coCoinRecord) {
        long updateNumber = db.updateRecord(coCoinRecord);
        if (updateNumber <= 0) {
            if (BuildConfig.DEBUG) {
                if (BuildConfig.DEBUG) Log.d("UtcCoin", "recordManager.updateRecord " + coCoinRecord.toString() + " F");
            }
            UtcCoinToast.getInstance().showToast(R.string.update_failed_locale, SuperToast.Background.RED);
        } else {
            if (BuildConfig.DEBUG) {
                if (BuildConfig.DEBUG) Log.d("UtcCoin", "recordManager.updateRecord " + coCoinRecord.toString() + " S");
            }
            p = RECORDS.size() - 1;
            for (; p >= 0; p--) {
                if (RECORDS.get(p).getId() == coCoinRecord.getId()) {
                    SUM -= (int)RECORDS.get(p).getMoney();
                    SUM += (int) coCoinRecord.getMoney();
                    RECORDS.get(p).set(coCoinRecord);
                    break;
                }
            }
            coCoinRecord.setIsUploaded(false);
//            User user = BmobUser
//                    .getCurrentUser(UtcCoinApplication.getAppContext(), User.class);
//            if (user != null) {
//                // already login
//                if (coCoinRecord.getLocalObjectId() != null) {
//                    // this coCoinRecord has been push to the server
//                    coCoinRecord.setUserId(user.getObjectId());
//                    coCoinRecord.update(UtcCoinApplication.getAppContext(),
//                            coCoinRecord.getLocalObjectId(), new UpdateListener() {
//                                @Override
//                                public void onSuccess() {
//                                    if (BuildConfig.DEBUG) {
//                                        if (BuildConfig.DEBUG) Log.d("UtcCoin", "recordManager.updateRecord update online " + coCoinRecord.toString() + " S");
//                                    }
//                                    coCoinRecord.setIsUploaded(true);
//                                    RECORDS.get(p).setIsUploaded(true);
//                                    db.updateRecord(coCoinRecord);
//                                    UtcCoinToast.getInstance().showToast(R.string.update_successfully_online, SuperToast.Background.BLUE);
//                                }
//
//                                @Override
//                                public void onFailure(int code, String msg) {
//                                    if (BuildConfig.DEBUG) {
//                                        if (BuildConfig.DEBUG) Log.d("UtcCoin", "recordManager.updateRecord update online " + coCoinRecord.toString() + " F");
//                                    }
//                                    if (BuildConfig.DEBUG) {
//                                        if (BuildConfig.DEBUG) Log.d("UtcCoin", "recordManager.updateRecord update online code" + code + " msg " + msg );
//                                    }
//                                    UtcCoinToast.getInstance().showToast(R.string.update_failed_online, SuperToast.Background.RED);
//                                }
//                            });
//                } else {
//                    // this coCoinRecord has not been push to the server
//                    coCoinRecord.setUserId(user.getObjectId());
//                    coCoinRecord.save(UtcCoinApplication.getAppContext(), new SaveListener() {
//                                @Override
//                                public void onSuccess() {
//                                    if (BuildConfig.DEBUG) {
//                                        if (BuildConfig.DEBUG) Log.d("UtcCoin", "recordManager.updateRecord save online " + coCoinRecord.toString() + " S");
//                                    }
//                                    coCoinRecord.setIsUploaded(true);
//                                    coCoinRecord.setLocalObjectId(coCoinRecord.getObjectId());
//                                    RECORDS.get(p).setIsUploaded(true);
//                                    RECORDS.get(p).setLocalObjectId(coCoinRecord.getObjectId());
//                                    db.updateRecord(coCoinRecord);
//                                    UtcCoinToast.getInstance().showToast(R.string.update_successfully_online, SuperToast.Background.BLUE);
//                                }
//                                @Override
//                                public void onFailure(int code, String msg) {
//                                    if (BuildConfig.DEBUG) {
//                                        if (BuildConfig.DEBUG) Log.d("UtcCoin", "recordManager.updateRecord save online " + coCoinRecord.toString() + " F");
//                                    }
//                                    if (BuildConfig.DEBUG) {
//                                        if (BuildConfig.DEBUG) Log.d("UtcCoin", "recordManager.updateRecord save online code" + code + " msg " + msg );
//                                    }
//                                    UtcCoinToast.getInstance().showToast(R.string.update_failed_online, SuperToast.Background.RED);
//                                }
//                            });
//                }
//            } else {
//                // has not login
//                db.updateRecord(coCoinRecord);
//                UtcCoinToast.getInstance().showToast(R.string.update_successfully_locale, SuperToast.Background.BLUE);
//            }
            db.updateRecord(coCoinRecord);
            UtcCoinToast.getInstance().showToast(R.string.update_successfully_locale, SuperToast.Background.BLUE);
        }
        return updateNumber;
    }

// update the records changed to server/////////////////////////////////////////////////////////////
    private static boolean isLastOne = false;
    public static long updateOldRecordsToServer() {
        long counter = 0;
        User user = BmobUser
                .getCurrentUser(UtcCoinApplication.getAppContext(), User.class);
        if (user != null) {
// already login////////////////////////////////////////////////////////////////////////////////////
            isLastOne = false;
            for (int i = 0; i < RECORDS.size(); i++) {
                if (i == RECORDS.size() - 1) isLastOne = true;
                final UtcCoinRecord coCoinRecord = RECORDS.get(i);
                if (!coCoinRecord.getIsUploaded()) {
// has been changed/////////////////////////////////////////////////////////////////////////////////
                    if (coCoinRecord.getLocalObjectId() != null) {
// there is an old coCoinRecord in server, we should update this coCoinRecord///////////////////////////////////
                        coCoinRecord.setUserId(user.getObjectId());
                        coCoinRecord.update(UtcCoinApplication.getAppContext(),
                                coCoinRecord.getLocalObjectId(), new UpdateListener() {
                                    @Override
                                    public void onSuccess() {
                                        if (BuildConfig.DEBUG) {
                                            if (BuildConfig.DEBUG) Log.d("UtcCoin", "recordManager.updateOldRecordsToServer update online " + coCoinRecord.toString() + " S");
                                        }
                                        coCoinRecord.setIsUploaded(true);
                                        coCoinRecord.setLocalObjectId(coCoinRecord.getObjectId());
                                        db.updateRecord(coCoinRecord);
// after updating, get the old records from server//////////////////////////////////////////////////
                                        if (isLastOne) getRecordsFromServer();
                                    }

                                    @Override
                                    public void onFailure(int code, String msg) {
                                        if (BuildConfig.DEBUG) {
                                            if (BuildConfig.DEBUG) Log.d("UtcCoin", "recordManager.updateOldRecordsToServer update online " + coCoinRecord.toString() + " F");
                                        }
                                        if (BuildConfig.DEBUG) {
                                            if (BuildConfig.DEBUG) Log.d("UtcCoin", "recordManager.updateOldRecordsToServer update online code" + code + " msg " + msg );
                                        }
                                    }
                                });
                    } else {
                        counter++;
                        coCoinRecord.setUserId(user.getObjectId());
                        coCoinRecord.save(UtcCoinApplication.getAppContext(), new SaveListener() {
                            @Override
                            public void onSuccess() {
                                if (BuildConfig.DEBUG) {
                                    if (BuildConfig.DEBUG) Log.d("UtcCoin", "recordManager.updateOldRecordsToServer save online " + coCoinRecord.toString() + " S");
                                }
                                coCoinRecord.setIsUploaded(true);
                                coCoinRecord.setLocalObjectId(coCoinRecord.getObjectId());
                                db.updateRecord(coCoinRecord);
// after updating, get the old records from server//////////////////////////////////////////////////
                                if (isLastOne) getRecordsFromServer();
                            }

                            @Override
                            public void onFailure(int code, String msg) {
                                if (BuildConfig.DEBUG) {
                                    if (BuildConfig.DEBUG) Log.d("UtcCoin", "recordManager.updateOldRecordsToServer save online " + coCoinRecord.toString() + " F");
                                }
                                if (BuildConfig.DEBUG) {
                                    if (BuildConfig.DEBUG) Log.d("UtcCoin", "recordManager.updateOldRecordsToServer save online code" + code + " msg " + msg );
                                }
                            }
                        });
                    }
                }
            }
        } else {

        }

        if (BuildConfig.DEBUG) {
            if (BuildConfig.DEBUG) Log.d("UtcCoin", "recordManager.updateOldRecordsToServer update " + counter + " records to server.");
        }

        if (RECORDS.size() == 0) getRecordsFromServer();

        return counter;
    }

    public static long updateTag(Tag tag) {
        int updateId = -1;
        if (BuildConfig.DEBUG) Log.d("UtcCoin",
                "Manager: Update tag: " + tag.toString());
        updateId = db.updateTag(tag);
        if (updateId == -1) {
            if (BuildConfig.DEBUG) Log.d("UtcCoin", "Update the above tag FAIL!");
        } else {
            if (BuildConfig.DEBUG) Log.d("UtcCoin", "Update the above tag SUCCESSFULLY!" + " - " + updateId);
            for (Tag t : TAGS) {
                if (t.getId() == tag.getId()) {
                    t.set(tag);
                    break;
                }
            }
            sortTAGS();
        }
        return updateId;
    }

//get records from server to local//////////////////////////////////////////////////////////////////
    private static long updateNum;
    public static long getRecordsFromServer() {
        updateNum = 0;
        BmobQuery<UtcCoinRecord> query = new BmobQuery<UtcCoinRecord>();
        query.addWhereEqualTo("userId",
                BmobUser.getCurrentUser(UtcCoinApplication.getAppContext(), User.class).getObjectId());
        query.setLimit(Integer.MAX_VALUE);
        query.findObjects(UtcCoinApplication.getAppContext(), new FindListener<UtcCoinRecord>() {
            @Override
            public void onSuccess(List<UtcCoinRecord> object) {
                if (BuildConfig.DEBUG) {
                    if (BuildConfig.DEBUG) Log.d("UtcCoin", "recordManager.getRecordsFromServer get " + object.size() + " records from server");
                }
                updateNum = object.size();
                for (UtcCoinRecord coCoinRecord : object) {
                    boolean exist = false;
                    for (int i = RECORDS.size() - 1; i >= 0; i--) {
                        if (coCoinRecord.getObjectId().equals(RECORDS.get(i).getLocalObjectId())) {
                            exist = true;
                            break;
                        }
                    }
                    if (!exist) {
                        UtcCoinRecord newUtcCoinRecord = new UtcCoinRecord();
                        newUtcCoinRecord.set(coCoinRecord);
                        newUtcCoinRecord.setId(-1);
                        RECORDS.add(newUtcCoinRecord);
                    }
                }

                Collections.sort(RECORDS, new Comparator<UtcCoinRecord>() {
                    @Override
                    public int compare(UtcCoinRecord lhs, UtcCoinRecord rhs) {
                        if (lhs.getCalendar().before(rhs.getCalendar())) {
                            return -1;
                        } else if (lhs.getCalendar().after(rhs.getCalendar())) {
                            return 1;
                        } else {
                            return 0;
                        }
                    }
                });

                db.deleteAllRecords();

                SUM = 0;
                for (int i = 0; i < RECORDS.size(); i++) {
                    RECORDS.get(i).setLocalObjectId(RECORDS.get(i).getObjectId());
                    RECORDS.get(i).setIsUploaded(true);
                    db.saveRecord(RECORDS.get(i));
                    SUM += (int)RECORDS.get(i).getMoney();
                }

                if (BuildConfig.DEBUG) {
                    if (BuildConfig.DEBUG) Log.d("UtcCoin", "recordManager.getRecordsFromServer save " + RECORDS.size() + " records");
                }
            }
            @Override
            public void onError(int code, String msg) {
                if (BuildConfig.DEBUG) {
                    if (BuildConfig.DEBUG) Log.d("UtcCoin", "recordManager.getRecordsFromServer error " + msg);
                }
            }
        });

        return updateNum;
    }

    public static int getCurrentMonthExpense() {
        Calendar calendar = Calendar.getInstance();
        Calendar left = UtcCoinUtil.GetThisMonthLeftRange(calendar);
        int monthSum = 0;
        for (int i = RECORDS.size() - 1; i >= 0; i--) {
            if (RECORDS.get(i).getCalendar().before(left)) break;
            monthSum += RECORDS.get(i).getMoney();
        }
        return monthSum;
    }

    public static List<UtcCoinRecord> queryRecordByTime(Calendar c1, Calendar c2) {
        List<UtcCoinRecord> list = new LinkedList<>();
        for (UtcCoinRecord coCoinRecord : RECORDS) {
            if (coCoinRecord.isInTime(c1, c2)) {
                list.add(coCoinRecord);
            }
        }
        return list;
    }

    public static List<UtcCoinRecord> queryRecordByCurrency(String currency) {
        List<UtcCoinRecord> list = new LinkedList<>();
        for (UtcCoinRecord coCoinRecord : RECORDS) {
            if (coCoinRecord.getCurrency().equals(currency)) {
                list.add(coCoinRecord);
            }
        }
        return list;
    }

    public static List<UtcCoinRecord> queryRecordByTag(int tag) {
        List<UtcCoinRecord> list = new LinkedList<>();
        for (UtcCoinRecord coCoinRecord : RECORDS) {
            if (coCoinRecord.getTag() == tag) {
                list.add(coCoinRecord);
            }
        }
        return list;
    }

    public static List<UtcCoinRecord> queryRecordByMoney(double money1, double money2, String currency) {
        List<UtcCoinRecord> list = new LinkedList<>();
        for (UtcCoinRecord coCoinRecord : RECORDS) {
            if (coCoinRecord.isInMoney(money1, money2, currency)) {
                list.add(coCoinRecord);
            }
        }
        return list;
    }

    public static List<UtcCoinRecord> queryRecordByRemark(String remark) {
        List<UtcCoinRecord> list = new LinkedList<>();
        for (UtcCoinRecord coCoinRecord : RECORDS) {
            if (UtcCoinUtil.IsStringRelation(coCoinRecord.getRemark(), remark)) {
                list.add(coCoinRecord);
            }
        }
        return list;
    }

    private void createTags() {
        saveTag(new Tag(-1, "Meal",                -1));
        saveTag(new Tag(-1, "Clothing & Footwear", 1));
        saveTag(new Tag(-1, "Home",                2));
        saveTag(new Tag(-1, "Traffic",             3));
        saveTag(new Tag(-1, "Vehicle Maintenance", 4));
        saveTag(new Tag(-1, "Book",                5));
        saveTag(new Tag(-1, "Hobby",               6));
        saveTag(new Tag(-1, "Internet",            7));
        saveTag(new Tag(-1, "Friend",              8));
        saveTag(new Tag(-1, "Education",           9));
        saveTag(new Tag(-1, "Entertainment",      10));
        saveTag(new Tag(-1, "Medical",            11));
        saveTag(new Tag(-1, "Insurance",          12));
        saveTag(new Tag(-1, "Donation",           13));
        saveTag(new Tag(-1, "Sport",              14));
        saveTag(new Tag(-1, "Snack",              15));
        saveTag(new Tag(-1, "Music",              16));
        saveTag(new Tag(-1, "Fund",               17));
        saveTag(new Tag(-1, "Drink",              18));
        saveTag(new Tag(-1, "Fruit",              19));
        saveTag(new Tag(-1, "Film",               20));
        saveTag(new Tag(-1, "Baby",               21));
        saveTag(new Tag(-1, "Partner",            22));
        saveTag(new Tag(-1, "Housing Loan",       23));
        saveTag(new Tag(-1, "Pet",                24));
        saveTag(new Tag(-1, "Telephone Bill",     25));
        saveTag(new Tag(-1, "Travel",             26));
        saveTag(new Tag(-1, "Lunch",              -2));
        saveTag(new Tag(-1, "Breakfast",          -3));
        saveTag(new Tag(-1, "MidnightSnack",      0));
        sortTAGS();
    }

    private void randomDataCreater() {

        Random random = new Random();

        List<UtcCoinRecord> createdUtcCoinRecords = new ArrayList<>();

        Calendar now = Calendar.getInstance();
        Calendar c = Calendar.getInstance();
        c.set(2015, 0, 1, 0, 0, 0);
        c.add(Calendar.SECOND, 1);

        while (c.before(now)) {
            for (int i = 0; i < RANDOM_DATA_NUMBER_ON_EACH_DAY; i++) {
                Calendar r = (Calendar)c.clone();
                int hour = random.nextInt(24);
                int minute = random.nextInt(60);
                int second = random.nextInt(60);

                r.set(Calendar.HOUR_OF_DAY, hour);
                r.set(Calendar.MINUTE, minute);
                r.set(Calendar.SECOND, second);
                r.add(Calendar.SECOND, 0);

                int tag = random.nextInt(TAGS.size());
                int expense = random.nextInt(RANDOM_DATA_EXPENSE_ON_EACH_DAY) + 1;

                UtcCoinRecord coCoinRecord = new UtcCoinRecord();
                coCoinRecord.setCalendar(r);
                coCoinRecord.setMoney(expense);
                coCoinRecord.setTag(tag);
                coCoinRecord.setCurrency("RMB");
                coCoinRecord.setRemark("备注：这里显示备注~");

                createdUtcCoinRecords.add(coCoinRecord);
            }
            c.add(Calendar.DATE, 1);
        }

        Collections.sort(createdUtcCoinRecords, new Comparator<UtcCoinRecord>() {
            @Override
            public int compare(UtcCoinRecord lhs, UtcCoinRecord rhs) {
                if (lhs.getCalendar().before(rhs.getCalendar())) {
                    return -1;
                } else if (lhs.getCalendar().after(rhs.getCalendar())) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });

        for (UtcCoinRecord coCoinRecord : createdUtcCoinRecords) {
            saveRecord(coCoinRecord);
        }
    }

    // Todo bug here
    private static void sortTAGS() {
        Collections.sort(TAGS, new Comparator<Tag>() {
            @Override
            public int compare(Tag lhs, Tag rhs) {
                if (lhs.getWeight() != rhs.getWeight()) {
                    return Integer.valueOf(lhs.getWeight()).compareTo(rhs.getWeight());
                } else if (!lhs.getName().equals(rhs.getName())) {
                    return lhs.getName().compareTo(rhs.getName());
                } else {
                    return Integer.valueOf(lhs.getId()).compareTo(rhs.getId());
                }
            }
        });
    }

}
