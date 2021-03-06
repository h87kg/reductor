package com.yheriatovych.reductor.example.reducers;

import com.yheriatovych.reductor.Reducer;
import com.yheriatovych.reductor.annotations.AutoReducer;
import com.yheriatovych.reductor.annotations.AutoReducer.Action;
import com.yheriatovych.reductor.example.model.NotesFilter;

@AutoReducer
public abstract class NotesFilterReducer implements Reducer<NotesFilter> {
    @Action("SET_FILTER")
    public NotesFilter setFilter(NotesFilter state, NotesFilter value) {
        return value;
    }
}
