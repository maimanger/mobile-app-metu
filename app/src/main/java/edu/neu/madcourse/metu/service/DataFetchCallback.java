package edu.neu.madcourse.metu.service;

public interface DataFetchCallback<T> {
    void onCallback(T value);
}
