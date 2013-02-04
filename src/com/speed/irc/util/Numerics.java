package com.speed.irc.util;

/**
 * Stores IRC numerics used by the API internal classes. Numerics are stored as
 * strings to allow easy comparison. (numerics are parsed as strings)
 * 
 * This file is part of Speed's IRC API.
 * 
 * Speed's IRC API is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * Speed's IRC API is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Speed's IRC API. If not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 * @author Shivam Mistry
 * 
 */
public interface Numerics {
	String WHO_RESPONSE = "352";
	String WHO_END = "315";
	String CHANNEL_NAMES = "353";
	String CHANNEL_NAMES_END = "366";
	String SERVER_SUPPORT = "005";
	String BANNED_FROM_CHANNEL = "474";
	String NOT_AN_OPERATOR = "482";
	String CHANNEL_MODES = "324";
}
