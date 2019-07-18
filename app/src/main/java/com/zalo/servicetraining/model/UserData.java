package com.zalo.servicetraining.model;

public class UserData {
        private int mId;
        private String mUsername;
        private String mAvatarPath;

        public int getId() {
            return mId;
        }

        public void setId(int mId) {
            this.mId = mId;
        }

        public String getUsername() {
            return mUsername;
        }

        public void setUsername(String username) {
            this.mUsername = username;
        }

        public String getAvatarPath() {
            return mAvatarPath;
        }

        public void setAvatarPath(String avatarPath) {
            this.mAvatarPath = avatarPath;
        }

        public UserData(int id, String username, String avatarPath) {;
            this.mId = id;
            this.mUsername = username;
            this.mAvatarPath = avatarPath;
        }
    }