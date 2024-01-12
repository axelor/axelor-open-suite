/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
import React from 'react';

export function useDebounce(cb, duration) {
  const timer = React.useRef(null);

  const clearTimer = () => timer.current && clearTimeout(timer.current);
  const setTimer = cb => (timer.current = setTimeout(cb, duration));

  React.useEffect(() => {
    return () => clearTimer();
  }, []);

  return (...args) => {
    clearTimer();
    setTimer(() => cb(...args));
  };
}

export function useFocusRef({ autoFocus }) {
  const inputRef = React.useRef();

  React.useEffect(() => {
    autoFocus === true && inputRef.current.focus();
  }, [autoFocus, inputRef]);

  return inputRef;
}
