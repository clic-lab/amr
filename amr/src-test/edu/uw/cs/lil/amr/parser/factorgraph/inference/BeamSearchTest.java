/*******************************************************************************
 * Copyright (C) 2011 - 2015 Yoav Artzi, All rights reserved.
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *******************************************************************************/
package edu.uw.cs.lil.amr.parser.factorgraph.inference;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import edu.cornell.cs.nlp.spf.data.sentence.Sentence;
import edu.cornell.cs.nlp.spf.data.situated.labeled.LabeledSituatedSentence;
import edu.cornell.cs.nlp.spf.data.situated.sentence.SituatedSentence;
import edu.cornell.cs.nlp.spf.mr.lambda.LogicalExpression;
import edu.cornell.cs.nlp.spf.parser.joint.model.IJointDataItemModel;
import edu.cornell.cs.nlp.spf.parser.joint.model.JointModel;
import edu.uw.cs.lil.amr.TestServices;
import edu.uw.cs.lil.amr.data.AMRMeta;
import edu.uw.cs.lil.amr.lambda.AMRServices;
import edu.uw.cs.lil.amr.parser.EvaluationResult;
import edu.uw.cs.lil.amr.parser.factorgraph.FactorGraph;
import edu.uw.cs.lil.amr.parser.factorgraph.assignmentgen.AssignmentGeneratorFactory;
import edu.uw.cs.lil.amr.parser.factorgraph.features.RelationSelectionalPreference;
import edu.uw.cs.lil.amr.parser.factorgraph.features.SurfaceFormFeature;
import edu.uw.cs.lil.amr.parser.factorgraph.nodes.CreateFactorGraph;

public class BeamSearchTest {

	public BeamSearchTest() {
		TestServices.init();
	}

	@Test
	public void test() {
		// "John ate his banana"
		final LogicalExpression exp = TestServices.getCategoryServices()
				.readSemantics(
						"(a:<id,<<e,t>,e>> !1 (lambda $0:e (and:<t*,t> (eat-01:<e,t> $0) "
								+ "(arg0:<e,<e,t>> $0 (a:<id,<<e,t>,e>> !3 (lambda $2:e (and:<t*,t> (person:<e,t> $2) (name:<e,<txt,t>> $2 john:txt))))) "
								+ "(arg1:<e,<e,t>> $0 (a:<id,<<e,t>,e>> !2 (lambda $1:e (and:<t*,t> (banana:<e,t> $1) (poss:<e,<e,t>> $1 (ref:<id,e> na:id)))))))))");
		final AssignmentGeneratorFactory factory = new AssignmentGeneratorFactory();
		final FactorGraph graph = CreateFactorGraph.of(exp, factory.create(exp),
				false);

		final JointModel<SituatedSentence<AMRMeta>, LogicalExpression, LogicalExpression> model = new JointModel.Builder<SituatedSentence<AMRMeta>, LogicalExpression, LogicalExpression>()
				.build();
		model.getTheta().set("RELPREF", "banana", "poss", "person", 1.1);
		model.getTheta().set("RELPREF", "banana", "poss", "eat-01", 1);
		final Sentence sentence = new Sentence("John ate his banana");
		final AMRMeta meta = new AMRMeta(sentence);
		final IJointDataItemModel<LogicalExpression, LogicalExpression> dim = model
				.createJointDataItemModel(
						new SituatedSentence<AMRMeta>(sentence, meta));

		new SurfaceFormFeature().createFactorJobs(graph, meta, dim)
				.parallelStream().forEach(r -> r.run());
		new RelationSelectionalPreference().createFactorJobs(graph, meta, dim)
				.parallelStream().forEach(r -> r.run());

		Assert.assertEquals(3, BeamSearch.of(graph, 3).first().size());
		Assert.assertEquals(2, BeamSearch.of(graph, 2).first().size());
		Assert.assertEquals(3, BeamSearch.of(graph, 10).first().size());

		final List<EvaluationResult> results = BeamSearch.of(graph, 1).first();

		Assert.assertEquals(1, BeamSearch.of(graph, 1).first().size());

		Assert.assertEquals(TestServices.getCategoryServices()
				.readSemantics("(a:<id,<<e,t>,e>> !1 (lambda $0:e (and:<t*,t> "
						+ "(eat-01:<e,t> $0) "
						+ "(arg0:<e,<e,t>> $0 (a:<id,<<e,t>,e>> !2 (lambda $1:e (and:<t*,t> "
						+ "(person:<e,t> $1) (name:<e,<txt,t>> $1 john:txt))))) "
						+ "(arg1:<e,<e,t>> $0 (a:<id,<<e,t>,e>> !3 (lambda $2:e (and:<t*,t> (banana:<e,t> $2) (poss:<e,<e,t>> $2 (ref:<id,e> !2)))))))))"),
				results.get(0).getResult());
		Assert.assertEquals(1.1, results.get(0).getScore(), 0.001);
	}

	@Test
	public void test2() {
		// "John ate his banana"
		final LogicalExpression exp = TestServices.getCategoryServices()
				.readSemantics(
						"(a:<id,<<e,t>,e>> !1 (lambda $0:e (and:<t*,t> (eat-01:<e,t> $0) "
								+ "(arg0:<e,<e,t>> $0 (a:<id,<<e,t>,e>> !3 (lambda $2:e (and:<t*,t> (person:<e,t> $2) (name:<e,<txt,t>> $2 john:txt))))) "
								+ "(arg1:<e,<e,t>> $0 (a:<id,<<e,t>,e>> !2 (lambda $1:e (and:<t*,t> (banana:<e,t> $1) (poss:<e,<e,t>> $1 (ref:<id,e> na:id)))))))))");
		final AssignmentGeneratorFactory factory = new AssignmentGeneratorFactory();
		final FactorGraph graph = CreateFactorGraph.of(exp, factory.create(exp),
				false);

		final JointModel<SituatedSentence<AMRMeta>, LogicalExpression, LogicalExpression> model = new JointModel.Builder<SituatedSentence<AMRMeta>, LogicalExpression, LogicalExpression>()
				.build();
		model.getTheta().set("RELPREF", "banana", "poss", "person", 1);
		model.getTheta().set("RELPREF", "banana", "poss", "eat-01", 1);
		final Sentence sentence = new Sentence("John ate his banana");
		final AMRMeta meta = new AMRMeta(sentence);
		final IJointDataItemModel<LogicalExpression, LogicalExpression> dim = model
				.createJointDataItemModel(
						new SituatedSentence<AMRMeta>(sentence, meta));

		new SurfaceFormFeature().createFactorJobs(graph, meta, dim)
				.parallelStream().forEach(r -> r.run());
		new RelationSelectionalPreference().createFactorJobs(graph, meta, dim)
				.parallelStream().forEach(r -> r.run());

		Assert.assertEquals(3, BeamSearch.of(graph, 10).first().size());

		final List<EvaluationResult> max = BeamSearch.of(graph, 2).first();
		final Set<LogicalExpression> top2 = max.stream().map(r -> r.getResult())
				.collect(Collectors.toSet());

		final Set<LogicalExpression> expected = new HashSet<>();
		expected.add(TestServices.getCategoryServices()
				.readSemantics("(a:<id,<<e,t>,e>> !1 (lambda $0:e (and:<t*,t> "
						+ "(eat-01:<e,t> $0) "
						+ "(arg0:<e,<e,t>> $0 (a:<id,<<e,t>,e>> !2 (lambda $1:e (and:<t*,t> "
						+ "(person:<e,t> $1) (name:<e,<txt,t>> $1 john:txt))))) "
						+ "(arg1:<e,<e,t>> $0 (a:<id,<<e,t>,e>> !3 (lambda $2:e (and:<t*,t> (banana:<e,t> $2) (poss:<e,<e,t>> $2 (ref:<id,e> !2)))))))))"));
		expected.add(TestServices.getCategoryServices()
				.readSemantics("(a:<id,<<e,t>,e>> !1 (lambda $0:e (and:<t*,t> "
						+ "(eat-01:<e,t> $0) "
						+ "(arg0:<e,<e,t>> $0 (a:<id,<<e,t>,e>> !2 (lambda $1:e (and:<t*,t> "
						+ "(person:<e,t> $1) (name:<e,<txt,t>> $1 john:txt))))) "
						+ "(arg1:<e,<e,t>> $0 (a:<id,<<e,t>,e>> !3 (lambda $2:e (and:<t*,t> (banana:<e,t> $2) (poss:<e,<e,t>> $2 (ref:<id,e> !1)))))))))"));

		Assert.assertEquals(expected, top2);
	}

	@Test
	public void test3() {
		// "John ate his banana"
		final LogicalExpression exp = TestServices.getCategoryServices()
				.readSemantics(
						"(a:<id,<<e,t>,e>> !1 (lambda $0:e (and:<t*,t> (eat-01:<e,t> $0) "
								+ "(arg0:<e,<e,t>> $0 (a:<id,<<e,t>,e>> !3 (lambda $2:e (and:<t*,t> (person:<e,t> $2) (name:<e,<txt,t>> $2 john:txt))))) "
								+ "(arg1:<e,<e,t>> $0 (a:<id,<<e,t>,e>> !2 (lambda $1:e (and:<t*,t> (banana:<e,t> $1) (poss:<e,<e,t>> $1 (ref:<id,e> na:id)))))))))");
		final AssignmentGeneratorFactory factory = new AssignmentGeneratorFactory();
		final FactorGraph graph = CreateFactorGraph.of(exp, factory.create(exp),
				false);

		final JointModel<SituatedSentence<AMRMeta>, LogicalExpression, LogicalExpression> model = new JointModel.Builder<SituatedSentence<AMRMeta>, LogicalExpression, LogicalExpression>()
				.build();
		final Sentence sentence = new Sentence("John ate his banana");
		final AMRMeta meta = new AMRMeta(sentence);
		final IJointDataItemModel<LogicalExpression, LogicalExpression> dim = model
				.createJointDataItemModel(
						new SituatedSentence<AMRMeta>(sentence, meta));

		new SurfaceFormFeature().createFactorJobs(graph, meta, dim)
				.parallelStream().forEach(r -> r.run());
		new RelationSelectionalPreference().createFactorJobs(graph, meta, dim)
				.parallelStream().forEach(r -> r.run());

		Assert.assertEquals(3, BeamSearch.of(graph, 10).first().size());
		Assert.assertEquals(0, BeamSearch.of(graph, 2).first().size());

		final List<EvaluationResult> max = BeamSearch.of(graph, 3).first();
		final Set<LogicalExpression> top3 = max.stream().map(r -> r.getResult())
				.collect(Collectors.toSet());

		final Set<LogicalExpression> expected = new HashSet<>();
		expected.add(TestServices.getCategoryServices()
				.readSemantics("(a:<id,<<e,t>,e>> !1 (lambda $0:e (and:<t*,t> "
						+ "(eat-01:<e,t> $0) "
						+ "(arg0:<e,<e,t>> $0 (a:<id,<<e,t>,e>> !2 (lambda $1:e (and:<t*,t> "
						+ "(person:<e,t> $1) (name:<e,<txt,t>> $1 john:txt))))) "
						+ "(arg1:<e,<e,t>> $0 (a:<id,<<e,t>,e>> !3 (lambda $2:e (and:<t*,t> (banana:<e,t> $2) (poss:<e,<e,t>> $2 (ref:<id,e> !2)))))))))"));
		expected.add(TestServices.getCategoryServices()
				.readSemantics("(a:<id,<<e,t>,e>> !1 (lambda $0:e (and:<t*,t> "
						+ "(eat-01:<e,t> $0) "
						+ "(arg0:<e,<e,t>> $0 (a:<id,<<e,t>,e>> !2 (lambda $1:e (and:<t*,t> "
						+ "(person:<e,t> $1) (name:<e,<txt,t>> $1 john:txt))))) "
						+ "(arg1:<e,<e,t>> $0 (a:<id,<<e,t>,e>> !3 (lambda $2:e (and:<t*,t> (banana:<e,t> $2) (poss:<e,<e,t>> $2 (ref:<id,e> !1)))))))))"));
		expected.add(TestServices.getCategoryServices()
				.readSemantics("(a:<id,<<e,t>,e>> !1 (lambda $0:e (and:<t*,t> "
						+ "(eat-01:<e,t> $0) "
						+ "(arg0:<e,<e,t>> $0 (a:<id,<<e,t>,e>> !2 (lambda $1:e (and:<t*,t> "
						+ "(person:<e,t> $1) (name:<e,<txt,t>> $1 john:txt))))) "
						+ "(arg1:<e,<e,t>> $0 (a:<id,<<e,t>,e>> !3 (lambda $2:e (and:<t*,t> (banana:<e,t> $2) (poss:<e,<e,t>> $2 (ref:<id,e> !3)))))))))"));

		Assert.assertEquals(expected, top3);
	}

	@Test
	public void test4() {
		final AssignmentGeneratorFactory assignmentGenFactory = new AssignmentGeneratorFactory();

		final Sentence sentence = new Sentence("John ate his banana");
		final LabeledSituatedSentence<AMRMeta, LogicalExpression> dataItem = new LabeledSituatedSentence<>(
				new SituatedSentence<>(sentence, new AMRMeta(sentence)),
				TestServices.getCategoryServices().readSemantics(
						"(a:<id,<<e,t>,e>> !1 (lambda $0:e (and:<t*,t> (eat-01:<e,t> $0) "
								+ "(arg0:<e,<e,t>> $0 (a:<id,<<e,t>,e>> !3 (lambda $2:e (and:<t*,t> (person:<e,t> $2) (name:<e,<txt,t>> $2 john:txt))))) "
								+ "(arg1:<e,<e,t>> $0 (a:<id,<<e,t>,e>> !2 (lambda $1:e (and:<t*,t> (banana:<e,t> $1) (poss:<e,<e,t>> $1 (ref:<id,e> !3)))))))))"));

		final LogicalExpression underspecified = AMRServices
				.underspecify(dataItem.getLabel());

		final FactorGraph graph = CreateFactorGraph.of(underspecified,
				assignmentGenFactory.create(underspecified), false);

		final JointModel<SituatedSentence<AMRMeta>, LogicalExpression, LogicalExpression> model = new JointModel.Builder<SituatedSentence<AMRMeta>, LogicalExpression, LogicalExpression>()
				.build();
		model.getTheta().set("RELPREF", "banana", "poss", "person", 1.1);
		final AMRMeta meta = new AMRMeta(sentence);
		final IJointDataItemModel<LogicalExpression, LogicalExpression> dim = model
				.createJointDataItemModel(
						new SituatedSentence<AMRMeta>(sentence, meta));

		new SurfaceFormFeature()
				.createFactorJobs(graph, dataItem.getSample().getState(), dim)
				.parallelStream().forEach(r -> r.run());
		new RelationSelectionalPreference()
				.createFactorJobs(graph, dataItem.getSample().getState(), dim)
				.parallelStream().forEach(r -> r.run());

		Assert.assertEquals(3, BeamSearch.of(graph, 10).first().size());
		Assert.assertEquals(3, BeamSearch.of(graph, 3).first().size());
		Assert.assertEquals(1, BeamSearch.of(graph, 2).first().size());

		final List<EvaluationResult> max = BeamSearch.of(graph, 1).first();
		Assert.assertEquals(1, max.size());

		Assert.assertEquals(TestServices.getCategoryServices()
				.readSemantics("(a:<id,<<e,t>,e>> !1 (lambda $0:e (and:<t*,t> "
						+ "(eat-01:<e,t> $0) "
						+ "(arg0:<e,<e,t>> $0 (a:<id,<<e,t>,e>> !2 (lambda $1:e (and:<t*,t> "
						+ "(person:<e,t> $1) (name:<e,<txt,t>> $1 john:txt))))) "
						+ "(arg1:<e,<e,t>> $0 (a:<id,<<e,t>,e>> !3 (lambda $2:e (and:<t*,t> (banana:<e,t> $2) (poss:<e,<e,t>> $2 (ref:<id,e> !2)))))))))"),
				max.get(0).getResult());
		Assert.assertEquals(1.1, max.get(0).getScore(), 0.001);
	}

	@Test
	public void test5() {
		// "John ate his banana"
		final LogicalExpression exp = TestServices.getCategoryServices()
				.readSemantics(
						"(a:<id,<<e,t>,e>> !1 (lambda $0:e (and:<t*,t> (eat-01:<e,t> $0) "
								+ "(arg0:<e,<e,t>> $0 (a:<id,<<e,t>,e>> !3 (lambda $2:e (and:<t*,t> (person:<e,t> $2) (name:<e,<txt,t>> $2 john:txt))))) "
								+ "(arg1:<e,<e,t>> $0 (a:<id,<<e,t>,e>> !2 (lambda $1:e (and:<t*,t> (banana:<e,t> $1) (poss:<e,<e,t>> $1 (ref:<id,e> na:id)))))))))");
		final AssignmentGeneratorFactory factory = new AssignmentGeneratorFactory();
		final FactorGraph graph = CreateFactorGraph.of(exp, factory.create(exp),
				false);

		final JointModel<SituatedSentence<AMRMeta>, LogicalExpression, LogicalExpression> model = new JointModel.Builder<SituatedSentence<AMRMeta>, LogicalExpression, LogicalExpression>()
				.build();
		model.getTheta().set("RELPREF", "banana", "poss", "person", 1);
		model.getTheta().set("RELPREF", "banana", "poss", "eat-01", 1);
		final Sentence sentence = new Sentence("John ate his banana");
		final AMRMeta meta = new AMRMeta(sentence);
		final IJointDataItemModel<LogicalExpression, LogicalExpression> dim = model
				.createJointDataItemModel(
						new SituatedSentence<AMRMeta>(sentence, meta));

		new SurfaceFormFeature().createFactorJobs(graph, meta, dim)
				.parallelStream().forEach(r -> r.run());
		new RelationSelectionalPreference().createFactorJobs(graph, meta, dim)
				.parallelStream().forEach(r -> r.run());

		Assert.assertEquals(3, BeamSearch.of(graph, 10).first().size());
		Assert.assertEquals(3, BeamSearch.of(graph, 3).first().size());
		Assert.assertEquals(2, BeamSearch.of(graph, 2).first().size());

		final List<EvaluationResult> max = BeamSearch.of(graph, 1, true)
				.first();
		Assert.assertEquals(1, max.size());

		final Set<LogicalExpression> maxExpressions = max.stream()
				.map((e) -> e.getResult()).collect(Collectors.toSet());

		final Set<LogicalExpression> expected = new HashSet<>();
		expected.add(TestServices.getCategoryServices()
				.readSemantics("(a:<id,<<e,t>,e>> !1 (lambda $0:e (and:<t*,t> "
						+ "(eat-01:<e,t> $0) "
						+ "(arg0:<e,<e,t>> $0 (a:<id,<<e,t>,e>> !2 (lambda $1:e (and:<t*,t> "
						+ "(person:<e,t> $1) (name:<e,<txt,t>> $1 john:txt))))) "
						+ "(arg1:<e,<e,t>> $0 (a:<id,<<e,t>,e>> !3 (lambda $2:e (and:<t*,t> (banana:<e,t> $2) (poss:<e,<e,t>> $2 (ref:<id,e> na:id)))))))))"));

		Assert.assertEquals(expected, maxExpressions);
	}

}
