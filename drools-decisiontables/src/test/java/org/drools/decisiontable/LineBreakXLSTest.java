package org.drools.decisiontable;

import com.sample.FactData;
import org.drools.kiesession.rulebase.InternalKnowledgeBase;
import org.drools.kiesession.rulebase.KnowledgeBaseFactory;
import org.junit.After;
import org.junit.Test;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.internal.builder.DecisionTableConfiguration;
import org.kie.internal.builder.DecisionTableInputType;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class LineBreakXLSTest {
    
    private KieSession ksession;

    @After
    public void tearDown() {
        if (ksession != null) {
            ksession.dispose();
        }
    }

    @Test
    public void makeSureAdditionalCodeLineEndsAreNotAdded() {

        DecisionTableConfiguration dtconf = KnowledgeBuilderFactory.newDecisionTableConfiguration();
        dtconf.setInputType(DecisionTableInputType.XLSX);
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newClassPathResource("testrule.drl.xlsx", getClass()), ResourceType.DTABLE, dtconf);
        if (kbuilder.hasErrors()) {
            fail(kbuilder.getErrors().toString());
        }
        InternalKnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addPackages(kbuilder.getKnowledgePackages());

        ksession = kbase.newKieSession();

        FactData fd = new FactData();
        fd.set値(-1);
        ksession.insert(fd);
        ksession.fireAllRules();

        assertThat(fd.getエラーメッセージ()).contains("値には0以上を指定してください。\n指定された値：");
    }

    @Test
    public void testMultipleLinesInAction() {

        // DROOLS-3805
        DecisionTableConfiguration dtconf = KnowledgeBuilderFactory.newDecisionTableConfiguration();
        dtconf.setInputType(DecisionTableInputType.XLS);
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newClassPathResource("MultiLinesInAction.drl.xls", getClass()), ResourceType.DTABLE, dtconf);
        if (kbuilder.hasErrors()) {
            fail(kbuilder.getErrors().toString());
        }
        InternalKnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addPackages(kbuilder.getKnowledgePackages());

        ksession = kbase.newKieSession();

        Person john = new Person("John");
        john.setAge(20);
        john.setAlive(true);
        ksession.insert(john);
        ksession.fireAllRules();

        assertThat(john.getAge()).isEqualTo(30);
        assertThat(john.isAlive()).isFalse();
    }

    @Test
    public void testMultipleLinesInCells() {
        // DROOLS-4358

        DecisionTableConfiguration dtconf = KnowledgeBuilderFactory.newDecisionTableConfiguration();
        dtconf.setInputType(DecisionTableInputType.XLS);
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newClassPathResource("MultiLinesInCells.drl.xls", getClass()), ResourceType.DTABLE, dtconf);
        if (kbuilder.hasErrors()) {
            fail(kbuilder.getErrors().toString());
        }
        InternalKnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addPackages(kbuilder.getKnowledgePackages());

        ksession = kbase.newKieSession();

        Person john = new Person("John");
        john.setAge(20);
        john.setAlive(true);
        ksession.insert(john);
        ksession.fireAllRules();

        assertThat(john.getAge()).isEqualTo(30);
        assertThat(john.getName()).isEqualTo("ssss\nxxxx");
    }
}
