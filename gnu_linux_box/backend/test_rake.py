text = """Telsa CEO Elon Musk on Wednesday announced on the company's s fourth-quarter earnings that Tesla is going to be shifting its product development focus to making the Tesla Bot, a humanoid robot, per Electrek.

Musk gave the world a sneak peek of the Tesla Bot in August 2020. According to CNBC, it was merely a human in a robot suit but the CEO expects to have a prototype this year.

“If you think about the economy, it is — the foundation of the economy is labor,” he said, per the report. “Capital equipment is distilled labor. So what happens if you don’t actually have a labor shortage? I’m not sure what an economy even means at that point. That’s what Optimus is about, so [it’s] very important.”

According to Fortune, he also confirmed that Tesla will not launch another car model in 2022. The most recent launch was Model Y in March 2020.
“Both last year and this year, if we were to introduce new vehicles, our total vehicle output would decrease,” he said to investors.
Those who were hoping for the release of the Cybertruck may have to wait another year. Musk added that the car is closing in on its production version though there are many challenges.

“I worry more about, like, how do we make the Cybertruck affordable,“ he told investors. “There’s a lot of new technology in the Cybertruck that will take some time to work through.”
He also said he would be shocked if his team didn’t achieve full self-driving cars this year. If Musk is successful in this, it would allow a Tesla care to serve as a “robotaxi” when it’s not being used, per the report."""

from rake_nltk import Rake

# Uses stopwords for english from NLTK, and all puntuation characters by
# default
r = Rake()

# Extraction given the text.
r.extract_keywords_from_text(text)

# To get keyword phrases ranked highest to lowest.
print(text)
print(r.get_ranked_phrases())

# To get keyword phrases ranked highest to lowest with scores.
print(r.get_ranked_phrases_with_scores())
