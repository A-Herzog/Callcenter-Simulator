/**
 * Copyright 2020 Alexander Herzog
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package mathtools.distribution;

import org.apache.commons.math3.distribution.BetaDistribution;

/**
 * Die Klasse <code>ExtBetaDistributionImpl</code> stellt eine Erweiterung der Klasse
 * <code>BetaDistributionImpl</code> dar, bei der der Träge der Dichte frei wählbar ist
 * und nicht auf das Intervall [0,1] festgelegt ist.
 * @author Alexander Herzog
 * @version 1.2
 * @see BetaDistribution
 */
public final class ExtBetaDistributionImpl extends BetaDistribution implements Cloneable {
	private static final long serialVersionUID = 787331141252463205L;

	/**
	 * Obere Grenze des Trägerbereichs der Wahrscheinlichkeitsmasse
	 */
	public final double domainLowerBound;

	/**
	 * Untere Grenze des Trägerbereichs der Wahrscheinlichkeitsmasse
	 */
	public final double domainUpperBound;

	/**
	 * Konstruktor der Klasse <code>ExtBetaDistributionImpl</code>
	 * @param domainLowerBound	Untere Grenze des Trägers der Dichte
	 * @param domainUpperBound	Obere Grenze des Trägers der Dichte
	 * @param alpha	Verteilungsparameter alpha
	 * @param beta	Verteilungsparameter beta
	 */
	public ExtBetaDistributionImpl(final double domainLowerBound, final double domainUpperBound, final double alpha, final double beta) {
		super(null,alpha,beta,BetaDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
		this.domainLowerBound=domainLowerBound;
		this.domainUpperBound=domainUpperBound;
	}

	/**
	 *
	 * Konstruktor der Klasse <code>ExtBetaDistributionImpl</code>
	 * @param domainLowerBound	Untere Grenze des Trägers der Dichte
	 * @param domainUpperBound	Obere Grenze des Trägers der Dichte
	 * @param alpha	Verteilungsparameter alpha
	 * @param beta	Verteilungsparameter beta
	 * @param inverseCumAccuracy	Genauigkeit für die Funktion <code>inverseCumulativeProbability</code>
	 */
	public ExtBetaDistributionImpl(final double domainLowerBound, final double domainUpperBound, final double alpha, final double beta, final double inverseCumAccuracy) {
		super(null,alpha,beta,inverseCumAccuracy);
		this.domainLowerBound=domainLowerBound;
		this.domainUpperBound=domainUpperBound;
	}

	@Override
	public double density(final double x) {
		if ((x<domainLowerBound) || (x>domainUpperBound)) return 0;
		return super.density((x-domainLowerBound)/(domainUpperBound-domainLowerBound));
	}

	@Override
	public double cumulativeProbability(final double x) {
		if (x<domainLowerBound) return 0;
		if (x>domainUpperBound) return 1;
		return super.cumulativeProbability((x-domainLowerBound)/(domainUpperBound-domainLowerBound));
	}

	@Override
	public double getNumericalMean() {
		final double alpha=getAlpha();
		final double beta=getBeta();
		final double a=getSupportLowerBound();
		final double b=getSupportUpperBound();
		return alpha/(alpha+beta)*(b-a)+a;
	}

	@Override
	public double getNumericalVariance() {
		final double alpha=getAlpha();
		final double beta=getBeta();
		final double a=getSupportLowerBound();
		final double b=getSupportUpperBound();
		return Math.pow(a-b,2)*alpha*beta/Math.pow(alpha+beta,2)/(1+alpha+beta);
	}

	@Override
	public ExtBetaDistributionImpl clone() {
		return new ExtBetaDistributionImpl(domainLowerBound,domainUpperBound,getAlpha(),getBeta());
	}

	@Override
	public double getSupportLowerBound() {return domainLowerBound;}

	@Override
	public double getSupportUpperBound() {return domainUpperBound;}

	@Override
	public boolean isSupportLowerBoundInclusive() {return true;}

	@Override
	public boolean isSupportUpperBoundInclusive() {return true;}
}
