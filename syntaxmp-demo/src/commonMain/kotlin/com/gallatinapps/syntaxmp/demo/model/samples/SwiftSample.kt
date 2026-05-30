package com.gallatinapps.syntaxmp.demo.model.samples

internal val SwiftSample = """
    import SwiftUI

    enum NoteStatus: String, Codable {
        case draft, review, published
    }

    struct NoteSummary: Identifiable {
        let id = UUID()
        var title: String
        var words: Int
        var isPinned: Bool
        var status: NoteStatus

        var label: String {
            "\(title) • \(max(1, words / 220)) min • \(status.rawValue)"
        }
    }

    @MainActor
    struct NoteSummaryRow: View {
        @State private var isExpanded = false
        @Environment(\.colorScheme) private var colorScheme
        @Binding var note: NoteSummary

        var body: some View {
            VStack(alignment: .leading, spacing: 8) {
                Toggle("Pinned", isOn: ${'$'}note.isPinned)
                Text(note.label)
                    .font(.headline)
                if isExpanded {
                    Text("Theme: \(colorScheme == .dark ? "dark" : "light")")
                        .foregroundStyle(.secondary)
                }
            }
            .onTapGesture { isExpanded.toggle() }
        }
    }

    #Preview {
        NoteSummaryRow(note: .constant(.init(
            title: "Launch plan",
            words: 1240,
            isPinned: true,
            status: .review
        )))
    }
""".trimIndent()
