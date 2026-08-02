package com.coopgoal.shared.config;

import com.coopgoal.contribution.domain.Contribution;
import com.coopgoal.contribution.domain.RecurringContribution;
import com.coopgoal.contribution.domain.RecurringFrequency;
import com.coopgoal.contribution.repository.ContributionRepository;
import com.coopgoal.contribution.repository.RecurringContributionRepository;
import com.coopgoal.goal.domain.FinancialGoal;
import com.coopgoal.goal.repository.FinancialGoalRepository;
import com.coopgoal.group.domain.CoopGroup;
import com.coopgoal.group.domain.Membership;
import com.coopgoal.group.domain.MembershipRole;
import com.coopgoal.group.repository.GroupRepository;
import com.coopgoal.group.repository.MembershipRepository;
import com.coopgoal.proposal.domain.Proposal;
import com.coopgoal.proposal.domain.ProposalType;
import com.coopgoal.proposal.repository.ProposalRepository;
import com.coopgoal.user.domain.User;
import com.coopgoal.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;

@Component
@Profile("dev")
public class DevelopmentDataLoader implements CommandLineRunner {
    private final UserRepository users;
    private final GroupRepository groups;
    private final MembershipRepository memberships;
    private final FinancialGoalRepository goals;
    private final ContributionRepository contributions;
    private final RecurringContributionRepository recurring;
    private final ProposalRepository proposals;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public DevelopmentDataLoader(UserRepository users, GroupRepository groups,
                                 MembershipRepository memberships, FinancialGoalRepository goals,
                                 ContributionRepository contributions,
                                 RecurringContributionRepository recurring,
                                 ProposalRepository proposals, PasswordEncoder passwordEncoder, Clock clock) {
        this.users = users;
        this.groups = groups;
        this.memberships = memberships;
        this.goals = goals;
        this.contributions = contributions;
        this.recurring = recurring;
        this.proposals = proposals;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (users.existsByEmailIgnoreCase("ana@coopgoal.dev")) return;

        User ana = users.save(User.create("Ana Silva", "ana@coopgoal.dev", passwordEncoder.encode("Senha123!")));
        User bruno = users.save(User.create("Bruno Costa", "bruno@coopgoal.dev", passwordEncoder.encode("Senha123!")));
        User carla = users.save(User.create("Carla Souza", "carla@coopgoal.dev", passwordEncoder.encode("Senha123!")));

        CoopGroup group = groups.save(CoopGroup.create("Viagem para o Chile",
                "Planejamento colaborativo das despesas", ana));
        Membership owner = memberships.save(Membership.create(group, ana, MembershipRole.OWNER));
        Membership admin = memberships.save(Membership.create(group, bruno, MembershipRole.ADMIN));
        memberships.save(Membership.create(group, carla, MembershipRole.MEMBER));

        LocalDate today = LocalDate.now(clock);
        FinancialGoal tickets = goals.save(FinancialGoal.create(group, "Passagens aéreas",
                "Passagens de ida e volta", new BigDecimal("12000.00"), today.plusMonths(6), ana));
        FinancialGoal lodging = goals.save(FinancialGoal.create(group, "Hospedagem",
                "Apartamento por dez dias", new BigDecimal("8000.00"), today.plusMonths(8), bruno));

        contributions.save(Contribution.create(tickets, owner, new BigDecimal("1500.00"),
                "Primeira contribuição", "dev-seed-ticket-1"));
        contributions.save(Contribution.create(tickets, admin, new BigDecimal("900.00"),
                "Reserva inicial", "dev-seed-ticket-2"));
        contributions.save(Contribution.create(lodging, owner, new BigDecimal("750.00"),
                "Entrada hospedagem", "dev-seed-lodging-1"));

        recurring.save(RecurringContribution.create(tickets, owner, new BigDecimal("300.00"),
                RecurringFrequency.MONTHLY, today.plusWeeks(2)));
        proposals.save(Proposal.create(tickets, bruno, ProposalType.CHANGE_TARGET_AMOUNT,
                "13500.00", "Ajuste após nova cotação", Instant.now(clock).plusSeconds(7 * 24 * 3600)));
    }
}
