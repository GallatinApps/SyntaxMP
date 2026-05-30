package com.gallatinapps.syntaxmp.demo.model.samples

internal val CppSample = """
    #include <algorithm>
    #include <format>
    #include <string>
    #include <string_view>
    #include <vector>

    enum class Status { Draft, Review, Published };

    struct NoteSummary {
        std::string title;
        int words = 0;
        bool pinned = false;
        Status status = Status::Draft;

        [[nodiscard]] int readingMinutes() const {
            return std::max(1, (words + 219) / 220);
        }

        [[nodiscard]] std::string label() const {
            return std::format("{} - {} min", title, readingMinutes());
        }
    };

    template <typename Range>
    auto reviewOnly(Range&& notes) {
        std::vector<NoteSummary> result;
        std::ranges::copy_if(notes, std::back_inserter(result), [](const auto& note) {
            return note.status == Status::Review && note.title.find("@docs") != std::string::npos;
        });
        return result;
    }

    void sortQueue(std::vector<NoteSummary>& notes) {
        std::ranges::sort(notes, {}, &NoteSummary::title);
    }
""".trimIndent()
