package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import barqsoft.footballscores.R;
import barqsoft.footballscores.Utility;
import barqsoft.footballscores.data.DatabaseContract;

/**
 * RemoteViewsService controlling the data being shown in the scrollable weather detail widget
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DetailWidgetRemoteViewsService extends RemoteViewsService {
    public final String LOG_TAG = DetailWidgetRemoteViewsService.class.getSimpleName();
    private static final String[] SCORES_COLUMNS = {
            DatabaseContract.scores_table._ID,
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL,
            DatabaseContract.scores_table.DATE_COL,
            DatabaseContract.scores_table.LEAGUE_COL,
            DatabaseContract.scores_table.MATCH_DAY,
            DatabaseContract.scores_table.TIME_COL
    };
    // these indices must match the projection
    public static final int COL_ID = 0;
    public static final int COL_HOME = 1;
    public static final int COL_AWAY = 2;
    public static final int COL_HOME_GOALS = 3;
    public static final int COL_AWAY_GOALS = 4;
    public static final int COL_DATE = 5;
    public static final int COL_LEAGUE = 6;
    public static final int COL_MATCHDAY = 7;
    public static final int COL_MATCHTIME = 8;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();
                //String location = Utility.getPreferredLocation(DetailWidgetRemoteViewsService.this);

                Uri dataByDateUri = DatabaseContract.scores_table.buildScoreWithDate();

                data = getContentResolver().query(dataByDateUri,
                        SCORES_COLUMNS,
                        null,
                        null,
                        DatabaseContract.scores_table.TIME_COL + " ASC");
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_detail_list_item);

                views.setTextViewText(R.id.widget_home_name, data.getString(COL_HOME));
                views.setTextViewText(R.id.widget_away_name, data.getString(COL_AWAY));
                views.setTextViewText(R.id.widget_score_textview, Utility.getScores(data.getInt(COL_HOME_GOALS), data.getInt(COL_AWAY_GOALS)));
                views.setImageViewResource(R.id.widget_home_crest, Utility.getTeamCrestByTeamName(data.getColumnName(COL_HOME)));
                views.setImageViewResource(R.id.widget_away_crest, Utility.getTeamCrestByTeamName(data.getColumnName(COL_AWAY)));


                //TODO: send intent to details view
                /*final Intent fillInIntent = new Intent();
                String locationSetting =
                        Utility.getPreferredLocation(DetailWidgetRemoteViewsService.this);
                Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                        locationSetting,
                        dateInMillis);
                fillInIntent.setData(weatherUri);
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);*/
                return views;

                /*
                String matchTime = cursor.getString(COL_MATCHTIME);
                mHolder.date.setText(matchTime);
                String[] matchTimeArray = matchTime.split(":");


                mHolder.match_id = cursor.getDouble(COL_ID);


                //Log.v(FetchScoreTask.LOG_TAG,mHolder.home_name.getText() + " Vs. " + mHolder.away_name.getText() +" id " + String.valueOf(mHolder.match_id));
                //Log.v(FetchScoreTask.LOG_TAG,String.valueOf(detail_match_id));
                LayoutInflater vi = (LayoutInflater) context.getApplicationContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View v = vi.inflate(R.layout.detail_fragment, null);
                ViewGroup container = (ViewGroup) view.findViewById(R.id.details_fragment_container);
                if(mHolder.match_id == detail_match_id)
                {
                    //Log.v(FetchScoreTask.LOG_TAG,"will insert extraView");

                    container.addView(v, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    TextView match_day = (TextView) v.findViewById(R.id.matchday_textview);
                    match_day.setText(Utility.getMatchDay(cursor.getInt(COL_MATCHDAY),
                            cursor.getInt(COL_LEAGUE)));
                    match_day.setContentDescription(Utility.getMatchDay(cursor.getInt(COL_MATCHDAY),
                            cursor.getInt(COL_LEAGUE)));

                    TextView league = (TextView) v.findViewById(R.id.league_textview);
                    league.setText(Utility.getLeague(cursor.getInt(COL_LEAGUE)));
                    league.setContentDescription("League: " + Utility.getLeague(cursor.getInt(COL_LEAGUE)));

                    Button share_button = (Button) v.findViewById(R.id.share_button);
                    share_button.setContentDescription("Share scores");
                    share_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //add Share Action
                            context.startActivity(createShareForecastIntent(mHolder.home_name.getText() + " "
                                    + mHolder.score.getText() + " " + mHolder.away_name.getText() + " "));
                        }
                    });
                }
                else
                {
                    container.removeAllViews();
                }

                */

            }

          /*  @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            private void setRemoteContentDescription(RemoteViews views, String description) {
                views.setContentDescription(R.id.widget_icon, description);
            }*/

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_detail_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(COL_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}