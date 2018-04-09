package com.axelor.apps.qms.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.qms.db.ImprovementForm;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;

public interface ImprovementFormService {
	/**
	 * Fetch next sequence value for improvement forms for the given company.
	 * @param company Improvement form's company
	 * @return The next sequence value, ready to be affected to improvement form reference.
	 * @throws AxelorException If no sequence is configured for improvement forms on the company.
	 */
	public String getSequence(Company company) throws AxelorException;

	// lifecycle
	/**
	 * Set status to confirmed and assign a reference.
	 * @param improvementForm Target form
	 * @throws AxelorException In case any business call fails
	 */
	@Transactional
	public void confirmImprovementForm(ImprovementForm improvementForm) throws AxelorException;

	@Transactional
	public void assignImprovementForm(ImprovementForm improvementForm) throws AxelorException;

	@Transactional
	public void analyzeImprovementForm(ImprovementForm improvementForm) throws AxelorException;

	@Transactional
	public void correctImprovementForm(ImprovementForm improvementForm) throws AxelorException;

	@Transactional
	public void closeImprovementForm(ImprovementForm improvementForm) throws AxelorException;

	@Transactional
	public void cancelImprovementForm(ImprovementForm improvementForm) throws AxelorException;

	@Transactional
	public void assessImprovementForm(ImprovementForm improvementForm) throws AxelorException;

	@Transactional
	public void reopenImprovementForm(ImprovementForm improvementForm) throws AxelorException;
}
