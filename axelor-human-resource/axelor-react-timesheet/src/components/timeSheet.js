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
import { ButtonToolbar, Glyphicon } from "react-bootstrap";
import { convertNumberToTime } from "./container";
import "./main.css";
import CellComponent from "./cellComponent";
import Cell from "./cell";
import { translate } from "./cellComponent";

class TimeSheet extends Component {
  isToday(date) {
    return moment(date, "YYYY-MM-DD").isSame(moment().format("YYYY-MM-DD"));
  }

  makeProjectTitle(tasks, field) {
    const task = {};
    Object.keys(tasks).forEach((tsk) => {
      const temp = tasks[tsk];
      let title = `${temp[field] || ""}`;
      task[tsk] = { title, ...temp };
    });
    return task;
  }

  isArrayEqual(arr1 = [], arr2 = []) {
    let flag = true;
    if (arr1.length === arr2.length) {
      arr1.forEach((element) => {
        let index = arr2.findIndex((a) => a === element);
        if (index === -1) {
          flag = false;
        }
      });
    } else {
      flag = false;
    }
    return flag;
  }

  componentDidUpdate(prevProps, prevState) {
    const navigationKey = this.props.navigationKey;
    const prevHeaders = Object.keys(prevProps.dateWise);
    const currHeaders = Object.keys(this.props.dateWise);
    if (!this.isArrayEqual(prevHeaders, currHeaders)) {
      const elements = document.getElementsByTagName("input");
      if (navigationKey === "end") {
        let targetElement = null;
        for (let i = 0; i < elements.length; i++) {
          const element = elements[i];
          if (!targetElement || element.tabIndex > targetElement.tabIndex) {
            targetElement = element;
          }
        }
        if (targetElement) {
          targetElement.focus();
          return;
        }
      } else {
        for (let i = 0; i < elements.length; i++) {
          const element = elements[i];
          if (element.tabIndex === 1) {
            element.focus();
            return;
          }
        }
      }
    }
  }

  renderSortingComponent(fieldName) {
    const { sortingList } = this.props;
    const item = sortingList.find((item) => item.fieldName === fieldName);
    if (item) {
      if (item.sort === "asc") {
        return (
          <Glyphicon glyph="triangle-top" style={{ top: 2, fontSize: 10 }} />
        );
      } else {
        return (
          <Glyphicon glyph="triangle-bottom" style={{ top: 2, fontSize: 10 }} />
        );
      }
    }
    return null;
  }

  render() {
    const { isLarge } = this.props;
    let divStyle = {
      overflowX: "auto",
      overflowY: "hidden",
      display: "inline-block",
      backgroundColor: "#FFFFFF",
      borderBottom: "1px solid #DDDDDD",
    };
    if (isLarge) {
      divStyle = {
        ...divStyle,
      };
    }
    return (
      <div className="timesheet-content" style={{ position: "relative" }}>
        <CellComponent
          header=""
          bodyStyleClass="text-content"
          footer={" "}
          data={this.props.tasks}
          isField={false}
          selectProject={(project) => this.props.toggleProjectList(project)}
          collapseProject={(projectId) => this.props.collapseProject(projectId)}
          style={{ flex: 1 }}
          showActivity={this.props.showActivity}
        />
        <Cell
          header="Task"
          bodyStyleClass="text-content"
          footer={" "}
          data={this.props.tasks}
          optionValue="projectTask"
          optionLabel="fullName"
          isField={false}
          style={{ flex: 1 }}
        />
        <div
          style={{
            ...divStyle,
            maxWidth: this.props.showActivity ? "60%" : "75%",
            overflowX: "auto",
            overflowY: "hidden",
          }}
        >
          <div
            className="duration-content"
            style={{ width: Object.keys(this.props.dateWise).length * 125 }}
          >
            {this.props.dateWise &&
              Object.keys(this.props.dateWise).map((r, i) => (
                <div
                  key={i}
                  style={{
                    width: 125,
                    display: "inline-block",
                    height: "100%",
                  }}
                >
                  <CellComponent
                    header={r}
                    isToday={this.isToday(r)}
                    isField={true}
                    index={i + 1}
                    noOfDays={this.props.rowDates.length}
                    data={this.props.dateWise[r]}
                    changeDuration={(d) => this.props.changeDuration(d)}
                    moveNext={() => this.props.moveNext()}
                    movePrev={() => this.props.movePrev()}
                    changeKeyPress={(navigationKey) =>
                      this.props.changeKeyPress(navigationKey)
                    }
                  />
                </div>
              ))}
          </div>
        </div>
        <div
          className="footer-content"
          style={{
            position: "relative",
            maxWidth: "10%",
            width: "5%",
            flex: 1,
          }}
        >
          <CellComponent
            bodyStyleClass="footer-context"
            cellBackGroundColor="#334250"
            header={translate("Total")}
            footer={convertNumberToTime(`${this.props.grandTotal}`)}
            data={this.props.taskTotal}
            isField={false}
          />
          <ButtonToolbar
            className="right-toolbar"
            style={{ display: !this.props.showButton && "none" }}
          >
            <button
              className="navigation"
              onClick={() => this.props.cleanEditor()}
              style={{
                fontWeight: "bolder",
                color: "white",
                display: "inline",
                marginRight: 5,
              }}
            >
              {translate("New")}
            </button>
            <button
              onClick={() => this.props.removeTimesheetLine()}
              className="navigation"
              style={{ fontWeight: "bolder", marginRight: 5, color: "white" }}
            >
              {translate("Delete")}
            </button>
          </ButtonToolbar>
        </div>
      </div>
    );
  }
}

export default TimeSheet;
