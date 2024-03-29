package com.nva.server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nva.server.entities.*;
import com.nva.server.repositories.*;
import com.vaadin.flow.spring.annotation.EnableVaadin;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

@SpringBootApplication
@EnableVaadin
@RequiredArgsConstructor
public class ServerApplication implements CommandLineRunner {
    private final ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FacultyRepository facultyRepository;
    @Autowired
    private MajorRepository majorRepository;
    @Autowired
    private EntranceMethodGroupRepository entranceMethodGroupRepository;
    @Autowired
    private EntranceMethodRepository entranceMethodRepository;
    @Autowired
    private TopicRepository topicRepository;
    @Autowired
    private ActionRepository actionRepository;
    @Autowired
    private ScopeRepository scopeRepository;
    @Autowired
    private InformationRepository informationRepository;
    @Autowired
    private EntranceScoreInformationRepository entranceScoreInformationRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Optional<User> existingUser = userRepository.findByEmail("admin@gmail.com");
        if (existingUser.isEmpty()) {
            User user = new User();
            user.setFirstName("Admin");
            user.setLastName("Account");
            user.setEmail("admin@gmail.com");
            user.setRole(Role.ROLE_ADMIN);
            user.setPassword(passwordEncoder.encode("admin"));

            userRepository.save(user);
        }
    }
//    @Bean
//    public CommandLineRunner loadData() {
//        return args -> {
//            loadUserData();
//            loadFacultyData();
//            loadMajorData();
//            loadEntranceMethodGroupData();
//            loadEntranceMethodData();
//            loadTopicData();
//            loadActionData();
//            loadScopeData();
//            loadInformationData();
//            loadEntranceScoreInformationData();
//        };
//    }

    private void loadEntranceScoreInformationData() {
        if (entranceScoreInformationRepository.count() == 0) {
            InputStream inputStream = getClass().getResourceAsStream("/data/entrance-score-information.json");
            try {
                List<EntranceScoreInformation> entranceScoreInformation = objectMapper.readValue(inputStream, new TypeReference<>() {});
                for (EntranceScoreInformation info : entranceScoreInformation) {
                    entranceScoreInformationRepository.save(info);
                }
            } catch (IOException ignored) {}
        }
    }

    private void loadInformationData() {
        if (informationRepository.count() == 0) {
            InputStream inputStream = getClass().getResourceAsStream("/data/general-information.json");
            try {
                List<Information> information = objectMapper.readValue(inputStream, new TypeReference<>() {});
                for (Information info : information) {
                    informationRepository.save(info);
                }
            } catch (IOException ignored) {}
        }
    }

    private void loadScopeData() {
        if (scopeRepository.count() == 0) {
            InputStream inputStream = getClass().getResourceAsStream("/data/scopes.json");
            try {
                List<Scope> scopes = objectMapper.readValue(inputStream, new TypeReference<>() {});
                for (Scope scope : scopes) {
                    scopeRepository.save(scope);
                }
            } catch (IOException ignored) {}
        }
    }

    private void loadActionData() {
        if (actionRepository.count() == 0) {
            InputStream inputStream = getClass().getResourceAsStream("/data/actions.json");
            try {
                List<Action> actions = objectMapper.readValue(inputStream, new TypeReference<>() {});
                for (Action action : actions) {
                    actionRepository.save(action);
                }
            } catch (IOException ignored) {}
        }
    }

    private void loadTopicData() {
        if (topicRepository.count() == 0) {
            InputStream inputStream = getClass().getResourceAsStream("/data/topics.json");
            try {
                List<Topic> topics = objectMapper.readValue(inputStream, new TypeReference<>() {});
                for (Topic topic : topics) {
                    topicRepository.save(topic);
                }
            } catch (IOException ignored) {}
        }
    }

    private void loadEntranceMethodData() {
        if (entranceMethodRepository.count() == 0) {
            InputStream inputStream = getClass().getResourceAsStream("/data/entrance-methods.json");
            try {
                List<EntranceMethod> entranceMethods = objectMapper.readValue(inputStream, new TypeReference<>() {});
                for (EntranceMethod entranceMethod : entranceMethods) {
                    entranceMethodRepository.save(entranceMethod);
                }
            } catch (IOException ignored) {}
        }
    }

    private void loadEntranceMethodGroupData() {
        if (entranceMethodGroupRepository.count() == 0) {
            InputStream inputStream = getClass().getResourceAsStream("/data/entrance-method-groups.json");
            try {
                List<EntranceMethodGroup> entranceMethodGroups = objectMapper.readValue(inputStream, new TypeReference<>() {});
                for (EntranceMethodGroup entranceMethodGroup : entranceMethodGroups) {
                    entranceMethodGroupRepository.save(entranceMethodGroup);
                }
            } catch (IOException ignored) {}
        }
    }

    private void loadMajorData() {
        if (majorRepository.count() == 0) {
            InputStream inputStream = getClass().getResourceAsStream("/data/majors.json");
            try {
                List<Major> majors = objectMapper.readValue(inputStream, new TypeReference<>() {});
                for (Major major : majors) {
                    majorRepository.save(major);
                }
            } catch (IOException ignored) {}
        }
    }

    private void loadFacultyData() {
        if (facultyRepository.count() == 0) {
            InputStream inputStream = getClass().getResourceAsStream("/data/faculties.json");
            try {
                List<Faculty> faculties = objectMapper.readValue(inputStream, new TypeReference<>() {});
                for (Faculty faculty : faculties) {
                    facultyRepository.save(faculty);
                }
            } catch (IOException ignored) {}
        }
    }

    private void loadUserData() {
        if (userRepository.count() == 0) {
            InputStream inputStream = getClass().getResourceAsStream("/data/users.json");
            try {
                List<User> users = objectMapper.readValue(inputStream, new TypeReference<>() {});
                for (User user : users) {
                    user.setPassword(passwordEncoder.encode(user.getPassword()));
                    userRepository.save(user);
                }
            } catch (IOException ignored) {}
        }
    }
}
