package channelsurfer.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.media.tv.TvContentRating;
import android.media.tv.TvContract;
import android.text.TextUtils;

import channelsurfer.TvContractUtils;

import java.util.Arrays;
import java.util.Objects;

/**
 * A convenience class to create and insert program information into the database.
 */
public final class Program implements Comparable<Program> {
    private static final long INVALID_LONG_VALUE = -1;
    private static final int INVALID_INT_VALUE = -1;

    private long mProgramId;
    private long mChannelId;
    private String mTitle;
    private String mEpisodeTitle;
    private int mSeasonNumber;
    private int mEpisodeNumber;
    private long mStartTimeUtcMillis;
    private long mEndTimeUtcMillis;
    private String mDescription;
    private String mLongDescription;
    private int mVideoWidth;
    private int mVideoHeight;
    private String mPosterArtUri;
    private String mThumbnailUri;
    private String[] mCanonicalGenres;
    private TvContentRating[] mContentRatings;
    private String mInternalProviderData;

    private Program() {
        mChannelId = INVALID_LONG_VALUE;
        mProgramId = INVALID_LONG_VALUE;
        mSeasonNumber = INVALID_INT_VALUE;
        mEpisodeNumber = INVALID_INT_VALUE;
        mStartTimeUtcMillis = INVALID_LONG_VALUE;
        mEndTimeUtcMillis = INVALID_LONG_VALUE;
        mVideoWidth = INVALID_INT_VALUE;
        mVideoHeight = INVALID_INT_VALUE;
    }

    public long getProgramId() {
        return mProgramId;
    }

    public long getChannelId() {
        return mChannelId;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getEpisodeTitle() {
        return mEpisodeTitle;
    }

    public int getSeasonNumber() {
        return mSeasonNumber;
    }

    public int getEpisodeNumber() {
        return mEpisodeNumber;
    }

    public long getStartTimeUtcMillis() {
        return mStartTimeUtcMillis;
    }

    public long getEndTimeUtcMillis() {
        return mEndTimeUtcMillis;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getLongDescription() {
        return mLongDescription;
    }

    public int getVideoWidth() {
        return mVideoWidth;
    }

    public int getVideoHeight() {
        return mVideoHeight;
    }

    public String[] getCanonicalGenres() {
        return mCanonicalGenres;
    }

    public TvContentRating[] getContentRatings() {
        return mContentRatings;
    }

    public String getPosterArtUri() {
        return mPosterArtUri;
    }

    public String getThumbnailUri() {
        return mThumbnailUri;
    }

    public String getInternalProviderData() {
        return mInternalProviderData;
    }

    public long getDuration() { return mEndTimeUtcMillis - mStartTimeUtcMillis; }

    public Program setStartTimeUtcMillis(long mStartTimeUtcMillis) {
        this.mStartTimeUtcMillis = mStartTimeUtcMillis;
        return this;
    }

    public Program setEndTimeUtcMillis(long mEndTimeUtcMillis) {
        this.mEndTimeUtcMillis = mEndTimeUtcMillis;
        return this;
    }

    public Program setChannelId(long mChannelId) {
        this.mChannelId = mChannelId;
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mChannelId, mStartTimeUtcMillis, mEndTimeUtcMillis,
                mTitle, mEpisodeTitle, mDescription, mLongDescription, mVideoWidth, mVideoHeight,
                mPosterArtUri, mThumbnailUri, mContentRatings, mCanonicalGenres, mSeasonNumber,
                mEpisodeNumber);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Program)) {
            return false;
        }
        Program program = (Program) other;
        return mChannelId == program.mChannelId
                && mStartTimeUtcMillis == program.mStartTimeUtcMillis
                && mEndTimeUtcMillis == program.mEndTimeUtcMillis
                && Objects.equals(mTitle, program.mTitle)
                && Objects.equals(mEpisodeTitle, program.mEpisodeTitle)
                && Objects.equals(mDescription, program.mDescription)
                && Objects.equals(mLongDescription, program.mLongDescription)
                && mVideoWidth == program.mVideoWidth
                && mVideoHeight == program.mVideoHeight
                && Objects.equals(mPosterArtUri, program.mPosterArtUri)
                && Objects.equals(mThumbnailUri, program.mThumbnailUri)
                && Arrays.equals(mContentRatings, program.mContentRatings)
                && Arrays.equals(mCanonicalGenres, program.mCanonicalGenres)
                && mSeasonNumber == program.mSeasonNumber
                && mEpisodeNumber == program.mEpisodeNumber;
    }

    @Override
    public int compareTo(Program other) {
        return Long.compare(mStartTimeUtcMillis, other.mStartTimeUtcMillis);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Program{")
                .append("programId=").append(mProgramId)
                .append(", channelId=").append(mChannelId)
                .append(", title=").append(mTitle)
                .append(", episodeTitle=").append(mEpisodeTitle)
                .append(", seasonNumber=").append(mSeasonNumber)
                .append(", episodeNumber=").append(mEpisodeNumber)
                .append(", startTimeUtcSec=").append(mStartTimeUtcMillis)
                .append(", endTimeUtcSec=").append(mEndTimeUtcMillis)
                .append(", videoWidth=").append(mVideoWidth)
                .append(", videoHeight=").append(mVideoHeight)
                .append(", posterArtUri=").append(mPosterArtUri)
                .append(", thumbnailUri=").append(mThumbnailUri)
                .append(", contentRatings=").append(mContentRatings)
                .append(", genres=").append(mCanonicalGenres);
        return builder.append("}").toString();
    }

    public void copyFrom(Program other) {
        if (this == other) {
            return;
        }

        mProgramId = other.mProgramId;
        mChannelId = other.mChannelId;
        mTitle = other.mTitle;
        mEpisodeTitle = other.mEpisodeTitle;
        mSeasonNumber = other.mSeasonNumber;
        mEpisodeNumber = other.mEpisodeNumber;
        mStartTimeUtcMillis = other.mStartTimeUtcMillis;
        mEndTimeUtcMillis = other.mEndTimeUtcMillis;
        mDescription = other.mDescription;
        mLongDescription = other.mLongDescription;
        mVideoWidth = other.mVideoWidth;
        mVideoHeight = other.mVideoHeight;
        mPosterArtUri = other.mPosterArtUri;
        mThumbnailUri = other.mThumbnailUri;
        mCanonicalGenres = other.mCanonicalGenres;
        mContentRatings = other.mContentRatings;
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        if (mChannelId != INVALID_LONG_VALUE) {
            values.put(TvContract.Programs.COLUMN_CHANNEL_ID, mChannelId);
        } else {
            values.putNull(TvContract.Programs.COLUMN_CHANNEL_ID);
        }
        if (!TextUtils.isEmpty(mTitle)) {
            values.put(TvContract.Programs.COLUMN_TITLE, mTitle);
        } else {
            values.putNull(TvContract.Programs.COLUMN_TITLE);
        }
        if (!TextUtils.isEmpty(mEpisodeTitle)) {
            values.put(TvContract.Programs.COLUMN_EPISODE_TITLE, mEpisodeTitle);
        } else {
            values.putNull(TvContract.Programs.COLUMN_EPISODE_TITLE);
        }
        if (mSeasonNumber != INVALID_INT_VALUE) {
            values.put(TvContract.Programs.COLUMN_SEASON_NUMBER, mSeasonNumber);
        } else {
            values.putNull(TvContract.Programs.COLUMN_SEASON_NUMBER);
        }
        if (mEpisodeNumber != INVALID_INT_VALUE) {
            values.put(TvContract.Programs.COLUMN_EPISODE_NUMBER, mEpisodeNumber);
        } else {
            values.putNull(TvContract.Programs.COLUMN_EPISODE_NUMBER);
        }
        if (!TextUtils.isEmpty(mDescription)) {
            values.put(TvContract.Programs.COLUMN_SHORT_DESCRIPTION, mDescription);
        } else {
            values.putNull(TvContract.Programs.COLUMN_SHORT_DESCRIPTION);
        }
        if (!TextUtils.isEmpty(mPosterArtUri)) {
            values.put(TvContract.Programs.COLUMN_POSTER_ART_URI, mPosterArtUri);
        } else {
            values.putNull(TvContract.Programs.COLUMN_POSTER_ART_URI);
        }
        if (!TextUtils.isEmpty(mThumbnailUri)) {
            values.put(TvContract.Programs.COLUMN_THUMBNAIL_URI, mThumbnailUri);
        } else {
            values.putNull(TvContract.Programs.COLUMN_THUMBNAIL_URI);
        }
        if (mCanonicalGenres != null && mCanonicalGenres.length > 0) {
            values.put(TvContract.Programs.COLUMN_CANONICAL_GENRE,
                    TvContract.Programs.Genres.encode(mCanonicalGenres));
        } else {
            values.putNull(TvContract.Programs.COLUMN_CANONICAL_GENRE);
        }
        if (mContentRatings != null && mContentRatings.length > 0) {
            values.put(TvContract.Programs.COLUMN_CONTENT_RATING,
                    TvContractUtils.contentRatingsToString(mContentRatings));
        } else {
            values.putNull(TvContract.Programs.COLUMN_CONTENT_RATING);
        }
        if (mStartTimeUtcMillis != INVALID_LONG_VALUE) {
            values.put(TvContract.Programs.COLUMN_START_TIME_UTC_MILLIS, mStartTimeUtcMillis);
        } else {
            values.putNull(TvContract.Programs.COLUMN_START_TIME_UTC_MILLIS);
        }
        if (mEndTimeUtcMillis != INVALID_LONG_VALUE) {
            values.put(TvContract.Programs.COLUMN_END_TIME_UTC_MILLIS, mEndTimeUtcMillis);
        } else {
            values.putNull(TvContract.Programs.COLUMN_END_TIME_UTC_MILLIS);
        }
        if (mVideoWidth != INVALID_INT_VALUE) {
            values.put(TvContract.Programs.COLUMN_VIDEO_WIDTH, mVideoWidth);
        } else {
            values.putNull(TvContract.Programs.COLUMN_VIDEO_WIDTH);
        }
        if (mVideoHeight != INVALID_INT_VALUE) {
            values.put(TvContract.Programs.COLUMN_VIDEO_HEIGHT, mVideoHeight);
        } else {
            values.putNull(TvContract.Programs.COLUMN_VIDEO_HEIGHT);
        }
        if (!TextUtils.isEmpty(mInternalProviderData)) {
            values.put(TvContract.Programs.COLUMN_INTERNAL_PROVIDER_DATA, mInternalProviderData);
        } else {
            values.putNull(TvContract.Programs.COLUMN_INTERNAL_PROVIDER_DATA);
        }
        return values;
    }

    public static Program fromCursor(Cursor cursor) {
        Builder builder = new Builder();
        int index = cursor.getColumnIndex(TvContract.Programs._ID);
        if (index >= 0 && !cursor.isNull(index)) {
            builder.setProgramId(cursor.getLong(index));
        }
        index = cursor.getColumnIndex(TvContract.Programs.COLUMN_CHANNEL_ID);
        if (index >= 0 && !cursor.isNull(index)) {
            builder.setChannelId(cursor.getLong(index));
        }
        index = cursor.getColumnIndex(TvContract.Programs.COLUMN_TITLE);
        if (index >= 0 && !cursor.isNull(index)) {
            builder.setTitle(cursor.getString(index));
        }
        index = cursor.getColumnIndex(TvContract.Programs.COLUMN_EPISODE_TITLE);
        if (index >= 0 && !cursor.isNull(index)) {
            builder.setEpisodeTitle(cursor.getString(index));
        }
        index = cursor.getColumnIndex(TvContract.Programs.COLUMN_SEASON_NUMBER);
        if(index >= 0 && !cursor.isNull(index)) {
            builder.setSeasonNumber(cursor.getInt(index));
        }
        index = cursor.getColumnIndex(TvContract.Programs.COLUMN_EPISODE_NUMBER);
        if(index >= 0 && !cursor.isNull(index)) {
            builder.setEpisodeNumber(cursor.getInt(index));
        }
        index = cursor.getColumnIndex(TvContract.Programs.COLUMN_SHORT_DESCRIPTION);
        if (index >= 0 && !cursor.isNull(index)) {
            builder.setDescription(cursor.getString(index));
        }
        index = cursor.getColumnIndex(TvContract.Programs.COLUMN_LONG_DESCRIPTION);
        if (index >= 0 && !cursor.isNull(index)) {
            builder.setLongDescription(cursor.getString(index));
        }
        index = cursor.getColumnIndex(TvContract.Programs.COLUMN_POSTER_ART_URI);
        if (index >= 0 && !cursor.isNull(index)) {
            builder.setPosterArtUri(cursor.getString(index));
        }
        index = cursor.getColumnIndex(TvContract.Programs.COLUMN_THUMBNAIL_URI);
        if (index >= 0 && !cursor.isNull(index)) {
            builder.setThumbnailUri(cursor.getString(index));
        }
        index = cursor.getColumnIndex(TvContract.Programs.COLUMN_CANONICAL_GENRE);
        if (index >= 0 && !cursor.isNull(index)) {
            builder.setCanonicalGenres(TvContract.Programs.Genres.decode(cursor.getString(index)));
        }
        index = cursor.getColumnIndex(TvContract.Programs.COLUMN_CONTENT_RATING);
        if (index >= 0 && !cursor.isNull(index)) {
            builder.setContentRatings(TvContractUtils.stringToContentRatings(cursor.getString(
                    index)));
        }
        index = cursor.getColumnIndex(TvContract.Programs.COLUMN_START_TIME_UTC_MILLIS);
        if (index >= 0 && !cursor.isNull(index)) {
            builder.setStartTimeUtcMillis(cursor.getLong(index));
        }
        index = cursor.getColumnIndex(TvContract.Programs.COLUMN_END_TIME_UTC_MILLIS);
        if (index >= 0 && !cursor.isNull(index)) {
            builder.setEndTimeUtcMillis(cursor.getLong(index));
        }
        index = cursor.getColumnIndex(TvContract.Programs.COLUMN_VIDEO_WIDTH);
        if (index >= 0 && !cursor.isNull(index)) {
            builder.setVideoWidth((int) cursor.getLong(index));
        }
        index = cursor.getColumnIndex(TvContract.Programs.COLUMN_VIDEO_HEIGHT);
        if (index >= 0 && !cursor.isNull(index)) {
            builder.setVideoHeight((int) cursor.getLong(index));
        }
        index = cursor.getColumnIndex(TvContract.Programs.COLUMN_INTERNAL_PROVIDER_DATA);
        if (index >= 0 && !cursor.isNull(index)) {
            builder.setInternalProviderData(cursor.getString(index));
        }
        return builder.build();
    }

    public static final class Builder {
        private final Program mProgram;

        public Builder() {
            mProgram = new Program();
        }

        public Builder(Program other) {
            mProgram = new Program();
            mProgram.copyFrom(other);
        }

        public Builder setProgramId(long programId) {
            mProgram.mProgramId = programId;
            return this;
        }

        public Builder setChannelId(long channelId) {
            mProgram.mChannelId = channelId;
            return this;
        }

        public Builder setTitle(String title) {
            mProgram.mTitle = title;
            return this;
        }

        public Builder setEpisodeTitle(String episodeTitle) {
            mProgram.mEpisodeTitle = episodeTitle;
            return this;
        }

        public Builder setSeasonNumber(int seasonNumber) {
            mProgram.mSeasonNumber = seasonNumber;
            return this;
        }

        public Builder setEpisodeNumber(int episodeNumber) {
            mProgram.mEpisodeNumber = episodeNumber;
            return this;
        }

        public Builder setStartTimeUtcMillis(long startTimeUtcMillis) {
            mProgram.mStartTimeUtcMillis = startTimeUtcMillis;
            return this;
        }

        public Builder setEndTimeUtcMillis(long endTimeUtcMillis) {
            mProgram.mEndTimeUtcMillis = endTimeUtcMillis;
            return this;
        }

        public Builder setDescription(String description) {
            mProgram.mDescription = description;
            return this;
        }

        public Builder setLongDescription(String longDescription) {
            mProgram.mLongDescription = longDescription;
            return this;
        }

        public Builder setVideoWidth(int width) {
            mProgram.mVideoWidth = width;
            return this;
        }

        public Builder setVideoHeight(int height) {
            mProgram.mVideoHeight = height;
            return this;
        }

        public Builder setContentRatings(TvContentRating[] contentRatings) {
            mProgram.mContentRatings = contentRatings;
            return this;
        }

        public Builder setPosterArtUri(String posterArtUri) {
            mProgram.mPosterArtUri = posterArtUri;
            return this;
        }

        public Builder setThumbnailUri(String thumbnailUri) {
            mProgram.mThumbnailUri = thumbnailUri;
            return this;
        }

        public Builder setCanonicalGenres(String[] genres) {
            mProgram.mCanonicalGenres = genres;
            return this;
        }

        public Builder setInternalProviderData(String data) {
            mProgram.mInternalProviderData = data;
            return this;
        }

        public Program build() {
            return mProgram;
        }
    }
}
