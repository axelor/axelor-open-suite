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
import React, { Component } from "react";
import moment from "moment";

import {
  validateDuration,
  convertNumberToTime,
  convertTimeToNumber,
} from "./container";

export function translate(str) {
  if (window.top && window.top._t && typeof str === "string") {
    return window.top._t(str);
  }
  return str;
}

export function dynamicSort(property) {
  var sortOrder = 1;
  if (property[0] === "-") {
    sortOrder = -1;
    property = property.substr(1);
  }
  return function (a, b) {
    var result =
      a[property] < b[property] ? -1 : a[property] > b[property] ? 1 : 0;
    return result * sortOrder;
  };
}

class CellComponent extends Component {
  constructor(props) {
    super(props);
    this.state = {
      data: {
        tasks: {},
      },
      isField: true,
    };
  }

  getData(taskIndex, duration) {
    const task = this.props.data.tasks[taskIndex];
    return {
      date: task.date,
      projectId: task.projectId,
      taskId: task.taskId,
      task: task.task,
      duration: parseFloat(duration === "" ? 0 : duration),
      projectTask: task.projectTask,
      projectTaskId: task.projectTask && task.projectTask.id,
    };
  }

  componentDidMount() {
    if (this.props.isField) {
      let data = {};
      Object.keys(this.props.data.tasks).forEach((d) => {
        let obj = this.props.data.tasks[d];
        obj.duration = convertNumberToTime(obj.duration);
        obj.isChanged = false;
        data[d] = obj;
      });
      this.setState({
        data: { ...this.props.data, tasks: data },
        isField: this.props.isField,
      });
    } else {
      this.setState({ data: this.props.data, isField: this.props.isField });
    }
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.isField) {
      let data = {};
      Object.keys(nextProps.data.tasks).forEach((d) => {
        let obj = nextProps.data.tasks[d];
        obj.duration = convertNumberToTime(obj.duration);
        obj.isChanged = false;
        data[d] = obj;
      });
      this.setState({
        data: { ...nextProps.data, tasks: data },
        isField: nextProps.isField,
      });
    } else {
      this.setState({ data: nextProps.data, isField: nextProps.isField });
    }
  }

  isSelected(selected) {
    if (selected) {
      return selected;
    }
    return false;
  }

  isNegative(key) {
    return this.state.data.tasks[key].hasNegative;
  }

  getTabIndex(index) {
    return index * this.props.noOfDays + this.props.index;
  }

  selectProject(data) {
    if (this.props.selectProject) {
      this.props.selectProject(data);
    }
  }

  collapseProject(id) {
    if (this.props.collapseProject) {
      this.props.collapseProject(id);
    }
  }

  changeDuration(e, task) {
    this.props.changeDuration(
      this.getData(task, convertTimeToNumber(e.target.value))
    );
  }

  getDisable(record) {
    return !record.isDisable && !record.isReadOnly ? false : true;
  }

  render() {
    const { isField, isToday, cellBackGroundColor, showActivity } = this.props;
    let backgroundColor = this.props.cellBackgroundColor || "#FFFFFF";
    backgroundColor = this.props.isToday ? "aliceblue" : backgroundColor;
    const styles = {
      backgroundColor:
        cellBackGroundColor !== undefined && `${cellBackGroundColor}`,
      color: cellBackGroundColor && "white",
      borderTop: cellBackGroundColor && "0px !important",
    };
    const style = this.props.style || {};

    return (
      <div
        style={{
          position: "relative",
          minWidth: this.state.isField ? 125 : "auto",
          height: "100%",
          borderTop: "1px solid #DDDDDD",
          ...style,
        }}
      >
        <div style={{ backgroundColor }}>
          {isField ? (
            <div className="cell-height cell-header" style={{ height: 50 }}>
              <div
                style={{
                  height: "inherit",
                  padding: 10,
                  paddingBottom: 20,
                  backgroundColor: isToday && "rgba(2,117,216, 0.298039)",
                }}
              >
                <span style={{ display: "block", fontWeight: "bold" }}>
                  {translate(
                    moment(this.props.header, "YYYY-MM-DD").format("dddd")
                  )}
                </span>
                <span style={{ display: "block", fontWeight: "bold" }}>
                  {translate(
                    moment(this.props.header, "YYYY-MM-DD").format("MMM")
                  )}
                  {` `}
                  {translate(
                    moment(this.props.header, "YYYY-MM-DD").format("DD")
                  )}
                </span>
              </div>
            </div>
          ) : (
            <div
              className="cell-height cell-header"
              style={{
                ...styles,
                padding: 10,
                paddingBottom: 20,
                height: 50,
                fontWeight: "bold",
              }}
              onClick={() => {
                if (this.props.headerClick) {
                  this.props.headerClick();
                }
              }}
            >
              <span>{translate(this.props.header)}</span>
              <span className="sorting-component">
                {this.props.sortingComponent}
              </span>
            </div>
          )}
          <div className={this.props.bodyStyleClass}>
            {this.props.isField
              ? Object.keys(this.state.data.tasks).map((task, index) => (
                  <div
                    className="cell-height"
                    key={index}
                    style={{
                      borderTop: "1px solid #DDDDDD",
                      backgroundColor: this.state.data.tasks[task].selected
                        ? "#abb9d3"
                        : index % 2 !== 0
                        ? "#FFFFFF"
                        : "#F2F2F2",
                    }}
                  >
                    <div
                      style={{
                        height: "inherit",
                        backgroundColor: isToday && "rgba(2,117,216, 0.3)",
                      }}
                    >
                      {task.indexOf("dummy") !== -1 ||
                      this.state.data.tasks[task].isDisable ? (
                        <span></span>
                      ) : (
                        <input
                          className="duration-input"
                          type="text"
                          onFocus={(e) => e.target.select()}
                          disabled={this.getDisable(
                            this.state.data.tasks[task]
                          )}
                          tabIndex={this.getTabIndex(index)}
                          style={{
                            backgroundColor: "transparent",
                            color: this.isNegative(task) && "red",
                          }} //index % 2 !== 0 ? '#FFFFFF' : '#F2F2F2'
                          value={this.state.data.tasks[task].duration}
                          onKeyPress={(e) => {
                            if (e.which === 13) {
                              const tabs =
                                document.getElementsByTagName("input");
                              for (let i = 0; i < tabs.length; i++) {
                                if (
                                  tabs[i].tabIndex === this.getTabIndex(index)
                                ) {
                                  const tabIndex = tabs[i].tabIndex;
                                  let target = null;
                                  for (let j = 0; j < tabs.length; j++) {
                                    if (e.shiftKey) {
                                      if (tabs[j].tabIndex < tabIndex) {
                                        if (
                                          !target ||
                                          tabs[j].tabIndex > target.tabIndex
                                        ) {
                                          target = tabs[j];
                                        }
                                      }
                                    } else {
                                      if (tabs[j].tabIndex > tabIndex) {
                                        if (
                                          !target ||
                                          tabs[j].tabIndex < target.tabIndex
                                        ) {
                                          target = tabs[j];
                                        }
                                      }
                                    }
                                  }
                                  if (target) {
                                    target.focus();
                                  } else {
                                    if (e.shiftKey) {
                                      this.changeDuration(e, task);
                                      this.props.changeKeyPress("end");
                                      this.props.movePrev();
                                    } else {
                                      this.changeDuration(e, task);
                                      this.props.changeKeyPress("start");
                                      this.props.moveNext();
                                    }
                                  }
                                }
                              }
                            }
                          }}
                          onKeyDown={(e) => {
                            if (e.key === "Tab") {
                              if (e.shiftKey) {
                                const tabIndex = e.target.tabIndex;
                                if (tabIndex === 1) {
                                  this.props.changeKeyPress("end");
                                  this.props.movePrev();
                                  e.preventDefault();
                                }
                              } else {
                                const tabs =
                                  document.getElementsByTagName("input");
                                for (let i = 0; i < tabs.length; i++) {
                                  if (
                                    tabs[i].tabIndex === this.getTabIndex(index)
                                  ) {
                                    const tabIndex = tabs[i].tabIndex;
                                    let target = null;
                                    for (let j = 0; j < tabs.length; j++) {
                                      if (tabs[j].tabIndex > tabIndex) {
                                        if (
                                          !target ||
                                          tabs[j].tabIndex < target.tabIndex
                                        ) {
                                          target = tabs[j];
                                        }
                                      }
                                    }
                                    if (!target) {
                                      this.changeDuration(e, task);
                                      this.props.moveNext();
                                      this.props.changeKeyPress("start");
                                      e.preventDefault();
                                    }
                                  }
                                }
                              }
                            }

                            let value = e.target.value;
                            if (value.length === 2 && !isNaN(e.key)) {
                              let data = this.state.data;
                              if (
                                value.substring(
                                  value.length - 1,
                                  value.length
                                ) === ":"
                              ) {
                                data.tasks[task].duration = `${value}`;
                              } else {
                                data.tasks[task].duration = `${value}:`;
                              }
                              this.setState({
                                data,
                              });
                            }
                          }}
                          onKeyUp={(e) => {
                            if (e.target.value.length === 3 && isNaN(e.key)) {
                              let data = this.state.data;
                              data.tasks[
                                task
                              ].duration = `${e.target.value.substring(0, 2)}`;
                              this.setState({
                                data,
                              });
                            }
                          }}
                          onChange={(e) => {
                            if (validateDuration(e.target.value)) {
                              let data = this.state.data;
                              if (
                                data.tasks[task].duration !== e.target.value
                              ) {
                                data.tasks[task].duration = e.target.value;
                                data.tasks[task].isChanged = true;
                                this.setState({
                                  data,
                                });
                              }
                            }
                          }}
                          onBlur={(e) => {
                            e.target.selectionStart = -1;
                            e.target.selectionEnd = -1;
                            if (this.state.data.tasks[task].isChanged) {
                              if (
                                e.target.value.length > 0 &&
                                validateDuration(e.target.value)
                              ) {
                                this.props.changeDuration(
                                  this.getData(
                                    task,
                                    convertTimeToNumber(e.target.value)
                                  )
                                );
                              } else {
                                let data = this.state.data;
                                data.tasks[task].duration = "00:00";
                                this.setState({
                                  data,
                                });
                              }
                            }
                          }}
                        />
                      )}
                    </div>
                  </div>
                ))
              : Object.keys(this.props.data).map((task, index) => (
                  <div
                    className="cell-height"
                    key={index}
                    style={{
                      ...styles,
                      //minWidth: 75,
                      borderTop: !cellBackGroundColor && "1px solid #DDDDDD",
                      overflowY: "auto",
                      overflow:
                        this.props.bodyStyleClass === "footer-context"
                          ? "hidden"
                          : "auto",
                      backgroundColor: cellBackGroundColor
                        ? styles.backgroundColor
                        : this.state.data[task].selected
                        ? "#abb9d3"
                        : index % 2 !== 0
                        ? "#FFFFFF"
                        : "#F2F2F2",
                    }}
                    onClick={() => this.selectProject(this.props.data[task])}
                  >
                    {task.startsWith("project") ? (
                      <div
                        style={{
                          padding: "2px 5px 2px 7px",
                          fontWeight: "bold",
                        }}
                      >
                        {showActivity && (
                          <span
                            className="collapse-icon"
                            onClick={() =>
                              this.collapseProject(
                                this.props.data[task].projectId
                              )
                            }
                          >
                            {this.props.data[task].isCollapse ? "+" : "-"}
                          </span>
                        )}
                        {this.props.data[task].title}
                      </div>
                    ) : (
                      <div style={{ padding: "2px 5px 2px 20px" }}>
                        {isNaN(this.props.data[task]) ||
                        this.props.data[task].title ? (
                          <span
                            style={{
                              display: showActivity ? "block" : "none",
                            }}
                          >
                            {this.props.data[task].title}
                          </span>
                        ) : (
                          convertNumberToTime(this.props.data[task])
                        )}
                      </div>
                    )}
                  </div>
                ))}
          </div>
          <div
            className={`task-footer`}
            style={{
              color: "white",
              borderTop: !cellBackGroundColor && "1px solid #DDDDDD",
              backgroundColor: "#334250",
              textAlign: "center",
            }}
          >
            {isToday ? (
              <div
                style={{
                  height: "inherit",
                  backgroundColor: "rgba(2,117,216, 0.3)",
                }}
              >
                <span
                  style={{
                    display: "block",
                    fontWeight: "bold",
                    paddingTop: 5,
                  }}
                >
                  {translate(this.props.footer) ||
                    convertNumberToTime(this.props.data.total)}
                </span>
              </div>
            ) : this.props.isField &&
              Object.keys(this.state.data.tasks).filter((task) =>
                task.startsWith("dummy")
              ).length === Object.keys(this.state.data.tasks).length ? (
              <span
                style={{ display: "block", fontWeight: "bold", paddingTop: 5 }}
              >
                &nbsp;
              </span>
            ) : (
              <span
                style={{ display: "block", fontWeight: "bold", paddingTop: 5 }}
              >
                {translate(this.props.footer) ||
                  convertNumberToTime(this.props.data.total)}
              </span>
            )}
          </div>
        </div>
      </div>
    );
  }
}

export default CellComponent;
