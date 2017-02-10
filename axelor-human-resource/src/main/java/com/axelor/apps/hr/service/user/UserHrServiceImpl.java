package com.axelor.apps.hr.service.user;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.General;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.HRConfig;
import com.axelor.apps.hr.db.PublicHolidayPlanning;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.auth.db.User;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class UserHrServiceImpl implements UserHrService {

	@Inject
	UserRepository userRepo;

	@Transactional
	public void createEmployee(User user) {
		if (user.getPartner() == null) {
			Beans.get(UserService.class).createPartner(user);
		}

		General config = Beans.get(GeneralService.class).getGeneral();

		Employee employee = new Employee();
		employee.setContactPartner(user.getPartner());
		employee.setTimeLoggingPreferenceSelect(config.getTimeLoggingPreferenceSelect());
		employee.setDailyWorkHours(config.getDailyWorkHours());
		employee.setNegativeValueLeave(config.getAllowNegativeLeaveEmployees());

		PublicHolidayPlanning planning = null;
		Company company = user.getActiveCompany();
		if (company != null) {
			HRConfig hrConfig = company.getHrConfig();
			if (hrConfig != null) {
				planning = hrConfig.getPublicHolidayPlanning();
			}
		}
		employee.setPublicHolidayPlanning(planning);

		employee.setUser(user);
		Beans.get(EmployeeRepository.class).save(employee);

		user.setEmployee(employee);
		userRepo.save(user);
	}
}
