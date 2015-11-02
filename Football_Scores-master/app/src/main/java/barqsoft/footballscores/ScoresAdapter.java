package barqsoft.footballscores;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by yehya khaled on 2/26/2015.
 */
public class ScoresAdapter extends CursorAdapter
{
    public static final int COL_ID = 0;
    public static final int COL_HOME = 1;
    public static final int COL_AWAY = 2;
    public static final int COL_HOME_GOALS = 3;
    public static final int COL_AWAY_GOALS = 4;
    public static final int COL_DATE = 5;
    public static final int COL_LEAGUE = 6;
    public static final int COL_MATCHDAY = 7;
    public static final int COL_MATCH_ID = 8;
    public static final int COL_MATCHTIME = 9;

    public double detail_match_id = 0;
    private String FOOTBALL_SCORES_HASHTAG;
    public ScoresAdapter(Context context, Cursor cursor, int flags)
    {
        super(context,cursor,flags);
        FOOTBALL_SCORES_HASHTAG = context.getString(R.string.hashtag);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent)
    {
        View mItem = LayoutInflater.from(context).inflate(R.layout.scores_list_item, parent, false);
        ViewHolder mHolder = new ViewHolder(mItem);
        mItem.setTag(mHolder);

        return mItem;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor)
    {
        final ViewHolder mHolder = (ViewHolder) view.getTag();
        mHolder.home_name.setText(cursor.getString(COL_HOME));
        mHolder.away_name.setText(cursor.getString(COL_AWAY));
        mHolder.home_name.setContentDescription(cursor.getString(COL_HOME) + context.getString(R.string.versus) + cursor.getString(COL_AWAY));

        String matchTime = cursor.getString(COL_MATCHTIME);
        mHolder.date.setText(matchTime);
        String[] matchTimeArray = matchTime.split(":");

        if(Integer.parseInt(matchTimeArray[1]) == 0) {
            mHolder.date.setContentDescription(context.getString(R.string.game_starts_at) + Integer.parseInt(matchTimeArray[0]) + context.getString(R.string.oclock));
        }
        else {
            mHolder.date.setContentDescription(context.getString(R.string.game_starts_at) + Integer.parseInt(matchTimeArray[0]) + " " + Integer.parseInt(matchTimeArray[1]));
        }

        mHolder.score.setText(Utility.getScores(context, cursor.getInt(COL_HOME_GOALS), cursor.getInt(COL_AWAY_GOALS)));

        if(cursor.getInt(COL_HOME_GOALS) == -1 || cursor.getInt(COL_AWAY_GOALS) == -1) {
            mHolder.score.setContentDescription(context.getString(R.string.score_not_available));
        }
        else {
            mHolder.score.setContentDescription(cursor.getString(COL_HOME_GOALS) + context.getString(R.string.to) + cursor.getString(COL_AWAY_GOALS));
        }

        mHolder.match_id = cursor.getDouble(COL_MATCH_ID);
        mHolder.home_crest.setImageResource(Utility.getTeamCrestByTeamName(context,
                cursor.getString(COL_HOME)));

        mHolder.away_crest.setImageResource(Utility.getTeamCrestByTeamName(context,
                cursor.getString(COL_AWAY)));

        LayoutInflater vi = (LayoutInflater) context.getApplicationContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(R.layout.detail_fragment, null);
        ViewGroup container = (ViewGroup) view.findViewById(R.id.details_fragment_container);
        if(mHolder.match_id == detail_match_id)
        {
            container.addView(v, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            TextView match_day = (TextView) v.findViewById(R.id.matchday_textview);
            match_day.setText(Utility.getMatchDay(context, cursor.getInt(COL_MATCHDAY),
                    cursor.getInt(COL_LEAGUE)));
            match_day.setContentDescription(Utility.getMatchDay(context, cursor.getInt(COL_MATCHDAY),
                    cursor.getInt(COL_LEAGUE)));

            TextView league = (TextView) v.findViewById(R.id.league_textview);
            league.setText(Utility.getLeague(context, cursor.getInt(COL_LEAGUE)));
            league.setContentDescription(context.getString(R.string.league) + Utility.getLeague(context, cursor.getInt(COL_LEAGUE)));

            Button share_button = (Button) v.findViewById(R.id.share_button);
            share_button.setContentDescription(context.getString(R.string.share_scores));
            share_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //add Share Action
                    context.startActivity(createShareScoresIntent(mHolder.home_name.getText() + " "
                            + mHolder.score.getText() + " " + mHolder.away_name.getText() + " "));
                }
            });
        }
        else
        {
            container.removeAllViews();
        }

    }
    public Intent createShareScoresIntent(String ShareText) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, ShareText + FOOTBALL_SCORES_HASHTAG);
        return shareIntent;
    }

}
