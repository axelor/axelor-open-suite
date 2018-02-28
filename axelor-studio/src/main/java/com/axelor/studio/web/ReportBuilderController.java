/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.studio.web;

import java.net.URISyntaxException;

import org.apache.http.client.utils.URIBuilder;

import com.axelor.rpc.Context;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaView;
import com.axelor.meta.db.repo.MetaViewRepository;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.db.ReportBuilder;
import com.axelor.studio.db.ViewBuilder;
import com.axelor.studio.db.repo.ReportBuilderRepository;
import com.axelor.studio.service.ReportPrinterService;
import com.axelor.studio.service.builder.ReportBuilderService;
import com.google.common.base.Strings;
import com.google.inject.Inject;

public class ReportBuilderController {

	@Inject
	private ReportBuilderService builderService;

	@Inject
	private MetaViewRepository metaViewRepo;

	@Inject
	private ReportBuilderRepository reportBuilderRepo;

	@Inject
	private ReportPrinterService reportPrinter;

	public void generateTemplate(ActionRequest request, ActionResponse response) {

		ReportBuilder reportBuilder = request.getContext().asType(
				ReportBuilder.class);

		ViewBuilder viewBuilder = reportBuilder.getViewBuilder();

		if (viewBuilder != null) {
			MetaView metaView = viewBuilder.getMetaViewGenerated();
			if (metaView != null) {
				metaView = metaViewRepo.find(metaView.getId());

				String template = builderService.generateTemplate(metaView);

				response.setValue("htmlTemplate", template);
			} else {
				response.setFlash("No meta view found. Please run 'Apply update' to generate view");
			}

		} else {
			response.setFlash("No view found");
		}
	}

	public void download(ActionRequest request, ActionResponse response)
			throws URISyntaxException {

		Context context = request.getContext();
		String fileName = context.get("fileName").toString();
		String html = context.get("html").toString();
		Boolean printPageNo = (Boolean) context.get("printPageNo");

		downloadPdf(html, fileName, printPageNo, response);

	}

	private ActionResponse downloadPdf(String html, String fileName,
			Boolean printPageNo, ActionResponse response)
			throws URISyntaxException {

		URIBuilder builder = new URIBuilder("ws/htmlToPdf");
		builder.addParameter("html", html);
		builder.addParameter("fileName", fileName);
		if (printPageNo != null && printPageNo) {
			builder.addParameter("printPageNo", "true");
		}

		String url = builder.build().toString();
		response.setView(ActionView.define(I18n.get("Print")).add("html", url)
				.param("download", "true").param("fileName", fileName).map());

		return response;
	}

	public ActionResponse print(String builderIds, Long recordId,
			boolean canClose) throws URISyntaxException {

		ActionResponse response = new ActionResponse();
		if (Strings.isNullOrEmpty(builderIds)) {
			response.setFlash("No report builder found");
			return response;
		}

		String[] builders = builderIds.split(",");
		if (builders.length > 1) {
			return openSelector(response, builderIds, recordId);
		}

		Long builderId = Long.parseLong(builders[0]);
		ReportBuilder reportBuilder = reportBuilderRepo.find(builderId);
		if (reportBuilder == null) {
			response.setFlash("No report builder found");
			return response;
		}

		String[] html = reportPrinter.getHtml(reportBuilder, recordId);
		if (html.length == 1) {
			response.setFlash(html[0]);
			return response;
		}

		Boolean printPageNo = reportBuilder.getPrintPageNo();
		Boolean editHtml = reportBuilder.getEditHtml();

		return openReport(response, html, printPageNo, editHtml, canClose);
	}

	private ActionResponse openSelector(ActionResponse response,
			String builderIds, Long recordId) {

		response.setView(ActionView.define(I18n.get("Select report builder"))
				.model("com.axelor.studio.db.ReportBuilder")
				.add("form", "report-selector-form").param("popup", "true")
				.param("show-toolbar", "false").param("show-confirm", "false")
				.param("popup-save", "false").context("builderIds", builderIds)
				.context("_recordId", recordId).map());

		return response;

	}

	private ActionResponse openReport(ActionResponse response, String[] html,
			Boolean printPageNo, Boolean editHtml, boolean canClose)
			throws URISyntaxException {

		String fileName = html[0];
		if (editHtml) {
			response.setView(ActionView.define(I18n.get("Print"))
					.model("com.axelor.studio.db.ReportBuilder")
					.add("form", "report-edit-form").param("popup", "true")
					.param("show-toolbar", "false")
					.param("show-confirm", "false")
					.param("popup-save", "false").context("html", html[1])
					.context("fileName", fileName)
					.context("printPageNo", printPageNo).map());
			response.setCanClose(canClose);
			return response;
		} else {
			return downloadPdf(html[1], fileName, printPageNo, response);
		}
	}

}
