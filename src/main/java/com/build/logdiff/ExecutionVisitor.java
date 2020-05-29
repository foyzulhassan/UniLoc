package com.build.logdiff;

import org.apache.commons.text.diff.CommandVisitor;

public class ExecutionVisitor<T> implements CommandVisitor<T> {
	
	private final StringBuilder v;
    ExecutionVisitor() {
        v = new StringBuilder();
    }
    @Override
    public void visitInsertCommand(final T object) {
        //v.append(object);
    }
    @Override
    public void visitKeepCommand(final T object) {
        v.append(object);
    }
    @Override
    public void visitDeleteCommand(final T object) {
    }
    public String getString() {
        return v.toString();
    }

}
