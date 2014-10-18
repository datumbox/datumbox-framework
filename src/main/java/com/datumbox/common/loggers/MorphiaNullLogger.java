/* 
 * Copyright (C) 2014 Vasilis Vryniotis <bbriniotis at datumbox.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.datumbox.common.loggers;

import org.mongodb.morphia.logging.Logger;

/**
 *
 * @author Vasilis Vryniotis <bbriniotis at datumbox.com>
 */
public class MorphiaNullLogger implements Logger {

    @Override
    public boolean isTraceEnabled() {
        return false;
    }

    @Override
    public void trace(String msg) {
        
    }

    @Override
    public void trace(String format, Object... arg) {
        
    }

    @Override
    public void trace(String msg, Throwable t) {
        
    }

    @Override
    public boolean isDebugEnabled() {
        return false;
    }

    @Override
    public void debug(String msg) {
        
    }

    @Override
    public void debug(String format, Object... arg) {
        
    }

    @Override
    public void debug(String msg, Throwable t) {
        
    }

    @Override
    public boolean isInfoEnabled() {
        return false;
    }

    @Override
    public void info(String msg) {
        
    }

    @Override
    public void info(String format, Object... arg) {
        
    }

    @Override
    public void info(String msg, Throwable t) {
        
    }

    @Override
    public boolean isWarningEnabled() {
        return false;
    }

    @Override
    public void warning(String msg) {
        
    }

    @Override
    public void warning(String format, Object... arg) {
        
    }

    @Override
    public void warning(String msg, Throwable t) {
        
    }

    @Override
    public boolean isErrorEnabled() {
        return false;
    }

    @Override
    public void error(String msg) {
        
    }

    @Override
    public void error(String format, Object... arg) {
        
    }

    @Override
    public void error(String msg, Throwable t) {
        
    }
}