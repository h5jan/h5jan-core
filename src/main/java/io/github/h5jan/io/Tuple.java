/*-
 * 
 * Copyright 2019 Halliburton Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.h5jan.io;

/**
 * Generic Tuple class - an Object which holds two values
 * 
 * @author Nicholas Wright
 * @author Matthew Gerring removed dependency on serialization type in bean.
 *
 */
public class Tuple<L, R> {

	private L left = null;
	private R right = null;

	/**
	 * Default constructor
	 */
	public Tuple() {

	}

	/**
	 * Convenience constructor
	 * 
	 * @param left - left hand
	 * @param right - right hand
	 */
	public Tuple( L left, R right ) {
		this.left = left;
		this.right = right;
	}


	@Override
	public String toString() {
		return "Tuple [left=" + left + ", right=" + right + "]";
	}

	/**
	 * @return the left
	 */
	public L getLeft() {
		return left;
	}

	/**
	 * @param left
	 *            the left to set
	 */
	public void setLeft( L left ) {
		this.left = left;
	}

	/**
	 * @return the right
	 */
	public R getRight() {
		return right;
	}

	/**
	 * @param right
	 *            the right to set
	 */
	public void setRight( R right ) {
		this.right = right;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((left == null) ? 0 : left.hashCode());
		result = prime * result + ((right == null) ? 0 : right.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals( Object obj ) {
		if ( this == obj )
			return true;
		if ( obj == null )
			return false;
		if ( getClass() != obj.getClass() )
			return false;
		@SuppressWarnings("unchecked")
		Tuple<L, R> other = (Tuple<L, R>) obj;
		if ( left == null ) {
			if ( other.left != null )
				return false;
		} else if ( !left.equals( other.left ) )
			return false;
		if ( right == null ) {
			if ( other.right != null )
				return false;
		} else if ( !right.equals( other.right ) )
			return false;
		return true;
	}
}
